package com.corretorsipat.service;

import com.corretorsipat.io.ArquivoSipatReader;
import com.corretorsipat.model.*;
import com.corretorsipat.parser.LayoutSipat;
import com.corretorsipat.parser.SipatParser;
import com.corretorsipat.util.FormatUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Valida a entrada e identifica duplicidades preservando a primeira ocorrência física.
 */
public final class AnaliseSipatService {
    private final ArquivoSipatReader reader;

    /**
     * Cria o serviço com o leitor padrão de arquivos posicionais.
     */
    public AnaliseSipatService() {
        this(new ArquivoSipatReader());
    }

    /**
     * Permite injetar o leitor para testes isolados das regras de análise.
     */
    AnaliseSipatService(ArquivoSipatReader reader) {
        this.reader = reader;
    }

    /**
     * Registra somente controles divergentes para evitar ruído no relatório da análise.
     */
    private static void adicionarSeDiferente(List<Inconsistencia> lista, String campo, String esperado, String encontrado) {
        if (!esperado.equals(encontrado)) lista.add(new Inconsistencia(campo, esperado, encontrado));
    }

    /**
     * Lê, valida a estrutura e separa detalhes preservados dos excedentes duplicados.
     * A primeira ocorrência permanece no resultado porque o mapa mantém a ordem física de leitura.
     */
    public AnaliseSipat analisar(Path origem, ProgressoListener progresso)
            throws IOException, SipatValidationException {
        ProgressoListener listener = progresso == null ? ProgressoListener.NENHUM : progresso;
        listener.atualizar(5, "Lendo o arquivo SIPAT...");
        ArquivoSip arquivo = reader.ler(origem);
        List<RegistroSip> registros = arquivo.registros();
        if (registros.size() < 3)
            throw new SipatValidationException("O arquivo deve possuir cabeçalho, ao menos um detalhe e rodapé.");
        if (registros.getFirst().tipo() != '0')
            throw new SipatValidationException("A primeira linha deve ser um cabeçalho do tipo 0.");
        if (registros.getLast().tipo() != '9')
            throw new SipatValidationException("A última linha deve ser um rodapé do tipo 9.");
        // Impede que tipos intermediários inválidos sejam corrigidos como se fossem detalhes.
        for (int i = 1; i < registros.size() - 1; i++) {
            if (registros.get(i).tipo() != '1') {
                throw new SipatValidationException("Linha " + (i + 1) + ": registro de detalhe deve ser do tipo 1.");
            }
        }

        listener.atualizar(30, "Identificando protocolos repetidos...");
        Map<String, RegistroSip> primeiras = new LinkedHashMap<>();
        Map<String, List<LinhaRemovida>> removidasPorProtocolo = new LinkedHashMap<>();
        List<RegistroSip> preservados = new ArrayList<>();
        List<LinhaRemovida> removidas = new ArrayList<>();
        BigInteger somaOriginal = BigInteger.ZERO;
        BigInteger somaCorrigida = BigInteger.ZERO;

        // LinkedHashMap preserva a primeira ocorrência e a ordem original dos protocolos.
        for (int i = 1; i < registros.size() - 1; i++) {
            RegistroSip atual = registros.get(i);
            somaOriginal = somaOriginal.add(atual.valor());
            RegistroSip primeira = primeiras.putIfAbsent(atual.protocolo(), atual);
            if (primeira == null) {
                preservados.add(atual);
                somaCorrigida = somaCorrigida.add(atual.valor());
            } else {
                String classificacao = SipatParser.conteudoIgualExcetoSequencial(primeira.conteudo(), atual.conteudo())
                        ? LinhaRemovida.IDENTICA : LinhaRemovida.DIFERENTE;
                LinhaRemovida removida = new LinhaRemovida(
                        atual.linhaOriginal(), atual.protocolo(), primeira.linhaOriginal(), atual.valor(), "",
                        classificacao, LinhaRemovida.MOTIVO_DUPLICADO
                );
                removidas.add(removida);
                removidasPorProtocolo.computeIfAbsent(atual.protocolo(), chave -> new ArrayList<>()).add(removida);
            }
        }

        List<GrupoDuplicidade> grupos = new ArrayList<>();
        removidasPorProtocolo.forEach((protocolo, lista) -> grupos.add(new GrupoDuplicidade(
                protocolo, primeiras.get(protocolo).linhaOriginal(), lista)));

        RegistroSip cabecalho = registros.getFirst();
        RegistroSip rodape = registros.getLast();
        String cabQtd = SipatParser.extrair(cabecalho.conteudo(), LayoutSipat.CABECALHO_QUANTIDADE_INICIO, LayoutSipat.CABECALHO_QUANTIDADE_FIM);
        String cabInd = SipatParser.extrair(cabecalho.conteudo(), LayoutSipat.CABECALHO_INDICACOES_INICIO, LayoutSipat.CABECALHO_INDICACOES_FIM);
        String rodSeg = SipatParser.extrair(rodape.conteudo(), LayoutSipat.RODAPE_SEGURANCA_INICIO, LayoutSipat.RODAPE_SEGURANCA_FIM);
        String rodSoma = SipatParser.extrair(rodape.conteudo(), LayoutSipat.RODAPE_SOMA_INICIO, LayoutSipat.RODAPE_SOMA_FIM);
        int detalhes = registros.size() - 2;

        List<Inconsistencia> inconsistencias = new ArrayList<>();
        adicionarSeDiferente(inconsistencias, "Cabeçalho 29–33", FormatUtil.zeros(detalhes, 5), cabQtd);
        adicionarSeDiferente(inconsistencias, "Cabeçalho 39–43", FormatUtil.zeros(detalhes, 5), cabInd);
        adicionarSeDiferente(inconsistencias, "Rodapé 13–17", FormatUtil.zeros(detalhes * 2, 5), rodSeg);
        adicionarSeDiferente(inconsistencias, "Rodapé 18–35", FormatUtil.zeros(somaOriginal, 18), rodSoma);
        if (!"windows-1252".equalsIgnoreCase(arquivo.codificacao()))
            inconsistencias.add(new Inconsistencia("Codificação", "windows-1252", arquivo.codificacao()));
        if (arquivo.possuiBom()) inconsistencias.add(new Inconsistencia("BOM", "ausente", "presente"));
        if (arquivo.quebraLinha() != TipoQuebraLinha.CRLF)
            inconsistencias.add(new Inconsistencia("Quebra de linha", "CRLF", arquivo.quebraLinha().descricao()));
        if (!arquivo.terminaComQuebra()) inconsistencias.add(new Inconsistencia("CRLF final", "presente", "ausente"));

        // Confere o sequencial físico sem alterá-lo; a correção ocorrerá em outro serviço.
        int seqIncorretos = 0;
        for (int i = 0; i < registros.size(); i++) {
            String esperado = FormatUtil.zeros(i + 1, 5);
            if (!esperado.equals(registros.get(i).sequencial())) seqIncorretos++;
        }
        if (seqIncorretos > 0)
            inconsistencias.add(new Inconsistencia("Sequenciais físicos", "contínuos", seqIncorretos + " linha(s) divergente(s)"));

        listener.atualizar(100, "Análise concluída.");
        return new AnaliseSipat(
                origem.toAbsolutePath().normalize(), arquivo, registros.size(), detalhes, primeiras.size(),
                grupos.size(), removidas.size(), somaOriginal, somaCorrigida, cabQtd, cabInd, rodSeg, rodSoma,
                seqIncorretos, preservados, grupos, removidas, inconsistencias
        );
    }
}
