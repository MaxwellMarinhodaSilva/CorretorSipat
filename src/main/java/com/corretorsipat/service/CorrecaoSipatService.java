package com.corretorsipat.service;

import com.corretorsipat.io.ArquivoSipatWriter;
import com.corretorsipat.io.PlanoArquivosSaida;
import com.corretorsipat.log.ProcessamentoLog;
import com.corretorsipat.model.*;
import com.corretorsipat.parser.LayoutSipat;
import com.corretorsipat.parser.SipatParser;
import com.corretorsipat.report.RelatorioService;
import com.corretorsipat.util.FormatUtil;
import com.corretorsipat.util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Orquestra a reconstrução, gravação, validação pós-gravação e relatórios da correção. */
public final class CorrecaoSipatService {
    private final AnaliseSipatService analiseService = new AnaliseSipatService();
    private final ArquivoSipatWriter writer = new ArquivoSipatWriter();
    private final ValidacaoPosGravacaoService validacaoService = new ValidacaoPosGravacaoService();
    private final RelatorioService relatorioService = new RelatorioService();
    private final PlanoArquivosSaida planoService = new PlanoArquivosSaida();

    /** Produz uma mensagem utilizável quando a exceção original não possui detalhe. */
    private static String mensagem(Throwable ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Não foi possível concluir o processamento." : ex.getMessage();
    }

    /**
     * Reconstrói o SIP preservando somente a primeira ocorrência, atualiza os controles
     * permitidos e só confirma o resultado após reabrir e validar o arquivo gravado.
     */
    public ResultadoProcessamento corrigir(Path origem, ProgressoListener progresso) throws ProcessamentoException {
        ProgressoListener listener = progresso == null ? ProgressoListener.NENHUM : progresso;
        Instant inicio = Instant.now();
        ArquivosGerados arquivos = null;
        try {
            arquivos = planoService.criar(origem);
        } catch (Exception ex) {
            throw new ProcessamentoException(mensagem(ex), null, ex);
        }

        try (ProcessamentoLog log = new ProcessamentoLog(arquivos.log())) {
            try {
                log.escrever("Entrada: " + origem.toAbsolutePath());
                listener.atualizar(5, "Validando o arquivo de entrada...");
                AnaliseSipat analise = analiseService.analisar(origem, (p, etapa) -> listener.atualizar(Math.min(45, 5 + p * 40 / 100), etapa));
                log.escrever("Análise: " + analise.totalDetalhes() + " detalhes, " + analise.ocorrenciasExcedentes() + " excedente(s).");

                int quantidade = analise.detalhesPreservados().size();
                String qtd = FormatUtil.zeros(quantidade, 5);
                String seguranca = FormatUtil.zeros(quantidade * 2, 5);
                String soma = FormatUtil.zeros(analise.somaCorrigida(), 18);

                // Reconstrói a sequência física com cabeçalho, detalhes preservados e rodapé.
                List<RegistroSip> origemSelecionada = new ArrayList<>();
                origemSelecionada.add(analise.arquivoSip().registros().getFirst());
                origemSelecionada.addAll(analise.detalhesPreservados());
                origemSelecionada.add(analise.arquivoSip().registros().getLast());

                String cabecalho = origemSelecionada.getFirst().conteudo();
                cabecalho = SipatParser.substituir(cabecalho, LayoutSipat.CABECALHO_QUANTIDADE_INICIO, LayoutSipat.CABECALHO_QUANTIDADE_FIM, qtd);
                cabecalho = SipatParser.substituir(cabecalho, LayoutSipat.CABECALHO_INDICACOES_INICIO, LayoutSipat.CABECALHO_INDICACOES_FIM, qtd);
                String rodape = origemSelecionada.getLast().conteudo();
                rodape = SipatParser.substituir(rodape, LayoutSipat.RODAPE_SEGURANCA_INICIO, LayoutSipat.RODAPE_SEGURANCA_FIM, seguranca);
                rodape = SipatParser.substituir(rodape, LayoutSipat.RODAPE_SOMA_INICIO, LayoutSipat.RODAPE_SOMA_FIM, soma);

                List<String> linhas = new ArrayList<>();
                linhas.add(cabecalho);
                analise.detalhesPreservados().forEach(r -> linhas.add(r.conteudo()));
                linhas.add(rodape);
                // Renumera todas as linhas após as remoções, mantendo cinco posições fixas.
                int renumerados = 0;
                for (int i = 0; i < linhas.size(); i++) {
                    String novo = FormatUtil.zeros(i + 1, 5);
                    if (!novo.equals(origemSelecionada.get(i).sequencial())) renumerados++;
                    linhas.set(i, SipatParser.substituir(linhas.get(i), LayoutSipat.SEQUENCIAL_INICIO, LayoutSipat.SEQUENCIAL_FIM, novo));
                }

                listener.atualizar(60, "Gravando a cópia corrigida...");
                writer.escrever(arquivos.sipCorrigido(), linhas);
                log.escrever("SIP corrigido gravado: " + arquivos.sipCorrigido());
                listener.atualizar(75, "Reabrindo e validando a saída...");
                ResultadoValidacao validacao = validacaoService.validar(arquivos.sipCorrigido());
                // Nunca mantém uma saída que falhou na validação independente pós-gravação.
                if (!validacao.sucesso()) {
                    Files.deleteIfExists(arquivos.sipCorrigido());
                    throw new SipatValidationException("A validação independente encontrou divergências; a saída inválida foi removida.");
                }

                List<CorrecaoCampo> correcoes = List.of(
                        new CorrecaoCampo("Cabeçalho 29–33", analise.cabecalhoQuantidadeAnterior(), qtd),
                        new CorrecaoCampo("Cabeçalho 39–43", analise.cabecalhoIndicacoesAnterior(), qtd),
                        new CorrecaoCampo("Rodapé 13–17", analise.rodapeSegurancaAnterior(), seguranca),
                        new CorrecaoCampo("Rodapé 18–35", analise.rodapeSomaAnterior(), soma)
                );
                ResultadoProcessamento resultado = new ResultadoProcessamento(
                        LocalDateTime.now(), analise, linhas.size(), quantidade, analise.somaCorrigida(), qtd, seguranca,
                        FormatUtil.zeros(linhas.size(), 5), renumerados, correcoes, validacao,
                        Duration.between(inicio, Instant.now()), arquivos, HashUtil.sha256(origem), HashUtil.sha256(arquivos.sipCorrigido())
                );

                listener.atualizar(90, "Gerando relatórios...");
                relatorioService.salvar(resultado);
                log.escrever("Relatórios gerados: " + arquivos.relatorioTxt() + " | " + arquivos.relatorioCsv());
                log.escrever("Validação pós-gravação: SUCESSO.");
                log.escrever("Processamento concluído em " + resultado.duracao().toMillis() + " ms.");
                listener.atualizar(100, "Processamento concluído e validado.");
                return resultado;
            } catch (Exception ex) {
                // O detalhe técnico fica no log; a UI recebe uma exceção com caminho conhecido.
                log.erro(ex);
                throw new ProcessamentoException(mensagem(ex), arquivos.log(), ex);
            }
        } catch (ProcessamentoException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProcessamentoException(mensagem(ex), arquivos.log(), ex);
        }
    }
}
