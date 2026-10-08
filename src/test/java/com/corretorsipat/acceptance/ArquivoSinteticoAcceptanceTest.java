package com.corretorsipat.acceptance;

import com.corretorsipat.TestData;
import com.corretorsipat.io.ArquivoSipatReader;
import com.corretorsipat.io.ArquivoSipatWriter;
import com.corretorsipat.model.AnaliseSipat;
import com.corretorsipat.model.ResultadoProcessamento;
import com.corretorsipat.parser.LayoutSipat;
import com.corretorsipat.parser.SipatParser;
import com.corretorsipat.report.RelatorioService;
import com.corretorsipat.report.ResumoTextoService;
import com.corretorsipat.service.AnaliseSipatService;
import com.corretorsipat.service.CorrecaoSipatService;
import com.corretorsipat.ui.LinhasRemovidasTableModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Exercita, com 220 detalhes artificiais, o fluxo completo de correção e validação. */
class ArquivoSinteticoAcceptanceTest {
    private static final List<Integer> LINHAS_REMOVIDAS = List.of(36, 41, 45, 75, 95, 106, 131, 147, 214, 215, 216, 217, 218, 219, 220, 221);
    private static final Set<Integer> POSICOES_REMOVIDAS = Set.copyOf(LINHAS_REMOVIDAS.stream().map(linha -> linha - 1).toList());
    private static final Map<Integer, Integer> PROTOCOLO_DUPLICADO_POR_POSICAO = Map.ofEntries(
            Map.entry(35, 1), Map.entry(40, 2), Map.entry(44, 3), Map.entry(74, 4),
            Map.entry(94, 5), Map.entry(105, 6), Map.entry(130, 7), Map.entry(146, 8),
            Map.entry(213, 1), Map.entry(214, 2), Map.entry(215, 9), Map.entry(216, 10),
            Map.entry(217, 11), Map.entry(218, 12), Map.entry(219, 13), Map.entry(220, 14));
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    @TempDir Path temp;

    @Test void arquivoSinteticoProduzTodosOsResultadosObrigatorios() throws Exception {
        Path original = criarArquivoComDuplicidades();
        byte[] bytesOriginais = Files.readAllBytes(original);
        AnaliseSipat analise = new AnaliseSipatService().analisar(original, null);

        assertEquals(222, analise.totalLinhas());
        assertEquals(220, analise.totalDetalhes());
        assertEquals(204, analise.protocolosUnicos());
        assertEquals(14, analise.gruposDuplicados());
        assertEquals(16, analise.ocorrenciasExcedentes());
        assertEquals(LINHAS_REMOVIDAS, analise.linhasRemovidas().stream().map(l -> l.linhaOriginalRemovida()).toList());
        assertEquals(somaDasPosicoes(1, 220), analise.somaOriginal());
        assertEquals(somaDasPosicoesPreservadas(), analise.somaCorrigida());

        ResultadoProcessamento resultado = corrigirNaPastaTemporaria(original);
        assertArrayEquals(bytesOriginais, Files.readAllBytes(original));
        assertEquals(206, resultado.linhasDepois());
        assertEquals(204, resultado.detalhesDepois());
        assertEquals("00204", resultado.quantidadeCabecalho());
        assertEquals("00408", resultado.segurancaRodape());
        assertEquals("00206", resultado.sequencialFinal());
        assertEquals(somaDasPosicoesPreservadas(), resultado.somaFinal());
        assertTrue(resultado.validacao().sucesso());
        assertTrue(resultado.arquivos().pastaBase().startsWith(temp.toAbsolutePath().normalize()));
        assertTrue(Files.isRegularFile(resultado.arquivos().relatorioTxt()));
        assertTrue(Files.isRegularFile(resultado.arquivos().relatorioCsv()));
        assertTrue(Files.isRegularFile(resultado.arquivos().log()));

        byte[] bytesSaida = Files.readAllBytes(resultado.arquivos().sipCorrigido());
        String textoSaida = new String(bytesSaida, WINDOWS_1252);
        assertFalse(bytesSaida.length >= 3 && bytesSaida[0] == (byte) 0xef && bytesSaida[1] == (byte) 0xbb && bytesSaida[2] == (byte) 0xbf);
        assertTrue(textoSaida.endsWith("\r\n"));
        assertFalse(textoSaida.replace("\r\n", "").contains("\n"));
        AnaliseSipat finalAnalisado = new AnaliseSipatService().analisar(resultado.arquivos().sipCorrigido(), null);
        assertEquals(0, finalAnalisado.ocorrenciasExcedentes());
        assertTrue(finalAnalisado.inconsistencias().isEmpty());
        assertTrue(new ArquivoSipatReader().ler(resultado.arquivos().sipCorrigido()).registros()
                .stream().allMatch(registro -> registro.conteudo().length() == 800));

        ResultadoProcessamento repetido = corrigirNaPastaTemporaria(original);
        assertNotEquals(resultado.arquivos().sipCorrigido(), repetido.arquivos().sipCorrigido());
        assertArrayEquals(Files.readAllBytes(resultado.arquivos().sipCorrigido()), Files.readAllBytes(repetido.arquivos().sipCorrigido()));

        String txt = new RelatorioService().gerarTxt(resultado);
        String csv = new RelatorioService().gerarCsv(resultado);
        String resumo = new ResumoTextoService().gerar(resultado);
        LinhasRemovidasTableModel tabela = new LinhasRemovidasTableModel(resultado.analise().linhasRemovidas());
        assertEquals(16, tabela.getRowCount());
        assertEquals(36, tabela.getValueAt(0, 0));
        assertTrue(txt.contains("36, 41, 45, 75, 95, 106, 131, 147, 214, 215, 216, 217, 218, 219, 220, 221"));
        assertEquals(17, csv.lines().count());
        assertTrue(resumo.contains("Ocorrências removidas: 16"));
        assertTrue(resumo.contains("Sequencial final: 00206"));
    }

    @Test void arquivoSinteticoSemDuplicidadesComControlesInvalidosEReparado() throws Exception {
        Path entrada = criarArquivoSemDuplicidadesComControlesInvalidos();
        AnaliseSipat antes = new AnaliseSipatService().analisar(entrada, null);
        assertEquals("00220", antes.cabecalhoQuantidadeAnterior());
        assertEquals("00220", antes.cabecalhoIndicacoesAnterior());
        assertEquals(2, antes.sequenciaisIncorretos());
        assertEquals(0, antes.ocorrenciasExcedentes());

        ResultadoProcessamento resultado = corrigirNaPastaTemporaria(entrada);
        assertEquals(2, resultado.sequenciaisRenumerados());
        assertEquals("00204", resultado.quantidadeCabecalho());
        assertTrue(resultado.validacao().sucesso());
        AnaliseSipat depois = new AnaliseSipatService().analisar(resultado.arquivos().sipCorrigido(), null);
        assertTrue(depois.inconsistencias().isEmpty());
    }

    private Path criarArquivoComDuplicidades() throws Exception {
        List<String> detalhes = new ArrayList<>();
        int proximoProtocoloUnico = 15;
        for (int posicao = 1; posicao <= 220; posicao++) {
            Integer duplicado = PROTOCOLO_DUPLICADO_POR_POSICAO.get(posicao);
            int protocolo = duplicado != null ? duplicado : posicao <= 14 ? posicao : proximoProtocoloUnico++;
            detalhes.add(TestData.detalhe(protocoloSintetico(protocolo), valorSintetico(posicao), posicao + 1,
                    "DADO-SINTETICO-" + posicao));
        }
        assertEquals(205, proximoProtocoloUnico);
        return TestData.arquivo(temp.resolve("entrada-sintetica.SIP"), detalhes);
    }

    private Path criarArquivoSemDuplicidadesComControlesInvalidos() throws Exception {
        List<String> detalhes = new ArrayList<>();
        for (int posicao = 1; posicao <= 204; posicao++) {
            detalhes.add(TestData.detalhe(protocoloSintetico(posicao), valorSintetico(posicao), posicao + 1,
                    "DADO-SINTETICO-" + posicao));
        }
        Path entrada = TestData.arquivo(temp.resolve("entrada-sintetica-sem-duplicados.SIP"), detalhes);
        List<String> linhas = new ArrayList<>(new ArquivoSipatReader().ler(entrada).registros().stream().map(r -> r.conteudo()).toList());
        String cabecalho = SipatParser.substituir(linhas.getFirst(), LayoutSipat.CABECALHO_QUANTIDADE_INICIO, LayoutSipat.CABECALHO_QUANTIDADE_FIM, "00220");
        cabecalho = SipatParser.substituir(cabecalho, LayoutSipat.CABECALHO_INDICACOES_INICIO, LayoutSipat.CABECALHO_INDICACOES_FIM, "00220");
        linhas.set(0, cabecalho);
        linhas.set(80, SipatParser.substituir(linhas.get(80), LayoutSipat.SEQUENCIAL_INICIO, LayoutSipat.SEQUENCIAL_FIM, "99990"));
        linhas.set(160, SipatParser.substituir(linhas.get(160), LayoutSipat.SEQUENCIAL_INICIO, LayoutSipat.SEQUENCIAL_FIM, "99991"));
        new ArquivoSipatWriter().escrever(entrada, linhas);
        return entrada;
    }

    private ResultadoProcessamento corrigirNaPastaTemporaria(Path origem) throws Exception {
        String pastaBaseAnterior = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", temp.toString());
            return new CorrecaoSipatService().corrigir(origem, null);
        } finally {
            System.setProperty("user.dir", pastaBaseAnterior);
        }
    }

    private static String protocoloSintetico(int numero) {
        return String.format("900000000%05d", numero);
    }

    private static String valorSintetico(int posicao) {
        return String.format("%014d", 100_000L + posicao * 37L);
    }

    private static BigInteger somaDasPosicoes(int inicio, int fim) {
        BigInteger soma = BigInteger.ZERO;
        for (int posicao = inicio; posicao <= fim; posicao++) {
            soma = soma.add(new BigInteger(valorSintetico(posicao)));
        }
        return soma;
    }

    private static BigInteger somaDasPosicoesPreservadas() {
        BigInteger soma = BigInteger.ZERO;
        for (int posicao = 1; posicao <= 220; posicao++) {
            if (!POSICOES_REMOVIDAS.contains(posicao)) soma = soma.add(new BigInteger(valorSintetico(posicao)));
        }
        return soma;
    }
}
