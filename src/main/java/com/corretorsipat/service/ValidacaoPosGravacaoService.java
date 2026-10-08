package com.corretorsipat.service;

import com.corretorsipat.io.ArquivoSipatReader;
import com.corretorsipat.model.*;
import com.corretorsipat.parser.LayoutSipat;
import com.corretorsipat.parser.SipatParser;
import com.corretorsipat.util.FormatUtil;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reabre a saída gravada e confirma seus controles sem reutilizar a análise anterior. */
public final class ValidacaoPosGravacaoService {
    private final ArquivoSipatReader reader = new ArquivoSipatReader();

    /** Cria um item de auditoria com nome, estado e detalhe legível pela interface. */
    private static ItemValidacao item(String nome, boolean sucesso, String detalhe) {
        return new ItemValidacao(nome, sucesso, detalhe);
    }

    /** Recalcula os controles diretamente da saída persistida para evitar validar memória antiga. */
    public ResultadoValidacao validar(Path arquivoCorrigido) {
        List<ItemValidacao> itens = new ArrayList<>();
        try {
            ArquivoSip arquivo = reader.ler(arquivoCorrigido);
            List<RegistroSip> registros = arquivo.registros();
            boolean tipos = registros.size() >= 3 && registros.getFirst().tipo() == '0' && registros.getLast().tipo() == '9'
                    && registros.subList(1, registros.size() - 1).stream().allMatch(RegistroSip::detalhe);
            itens.add(item("Tipos 0/1/9", tipos, tipos ? "Estrutura válida." : "Estrutura de tipos inválida."));
            itens.add(item("Registros com 800 caracteres", registros.stream().allMatch(r -> r.conteudo().length() == 800),
                    registros.size() + " linha(s) verificadas."));

            List<RegistroSip> detalhes = registros.subList(1, registros.size() - 1);
            // A inserção no conjunto falha apenas quando um protocolo ainda está repetido.
            Set<String> protocolos = new HashSet<>();
            boolean unicos = detalhes.stream().allMatch(r -> protocolos.add(r.protocolo()));
            itens.add(item("Protocolos sem repetição", unicos, protocolos.size() + " protocolo(s) único(s)."));

            BigInteger soma = detalhes.stream().map(RegistroSip::valor).reduce(BigInteger.ZERO, BigInteger::add);
            String somaRodape = SipatParser.extrair(registros.getLast().conteudo(), LayoutSipat.RODAPE_SOMA_INICIO, LayoutSipat.RODAPE_SOMA_FIM);
            itens.add(item("Soma financeira do rodapé", FormatUtil.zeros(soma, 18).equals(somaRodape), "Calculada: " + soma));

            int quantidade = detalhes.size();
            String qtd = FormatUtil.zeros(quantidade, 5);
            String seguranca = FormatUtil.zeros(quantidade * 2, 5);
            RegistroSip cab = registros.getFirst();
            RegistroSip rod = registros.getLast();
            boolean quantidades = qtd.equals(SipatParser.extrair(cab.conteudo(),
                    LayoutSipat.CABECALHO_QUANTIDADE_INICIO, LayoutSipat.CABECALHO_QUANTIDADE_FIM))
                    && qtd.equals(SipatParser.extrair(cab.conteudo(),
                    LayoutSipat.CABECALHO_INDICACOES_INICIO, LayoutSipat.CABECALHO_INDICACOES_FIM))
                    && seguranca.equals(SipatParser.extrair(rod.conteudo(),
                    LayoutSipat.RODAPE_SEGURANCA_INICIO, LayoutSipat.RODAPE_SEGURANCA_FIM));
            itens.add(item("Quantidades de cabeçalho e rodapé", quantidades, "Detalhes: " + quantidade + "; segurança: " + seguranca + "."));

            // Cada posição física deve refletir o sequencial que será aceito pelo destino.
            boolean sequenciais = true;
            for (int i = 0; i < registros.size(); i++) {
                if (!FormatUtil.zeros(i + 1, 5).equals(registros.get(i).sequencial())) {
                    sequenciais = false;
                    break;
                }
            }
            itens.add(item("Sequenciais físicos contínuos", sequenciais, "Sequencial final: " + registros.getLast().sequencial() + "."));
            boolean fisico = "windows-1252".equalsIgnoreCase(arquivo.codificacao()) && !arquivo.possuiBom()
                    && arquivo.quebraLinha() == TipoQuebraLinha.CRLF && arquivo.terminaComQuebra();
            itens.add(item("Windows-1252 sem BOM e CRLF final", fisico,
                    arquivo.codificacao() + ", " + arquivo.quebraLinha().descricao() + ", CRLF final: " + arquivo.terminaComQuebra() + "."));
        } catch (Exception ex) {
            // Registra a impossibilidade de releitura como falha de validação, sem ocultá-la.
            itens.add(item("Reabertura independente", false, ex.getMessage()));
        }
        return new ResultadoValidacao(itens);
    }
}
