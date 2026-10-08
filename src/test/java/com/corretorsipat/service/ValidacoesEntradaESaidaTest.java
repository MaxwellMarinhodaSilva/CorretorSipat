package com.corretorsipat.service;

import com.corretorsipat.TestData;
import com.corretorsipat.io.ArquivoSipatWriter;
import com.corretorsipat.parser.SipatParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidacoesEntradaESaidaTest {
    @TempDir Path temp;

    @Test void rejeitaArquivoVazioOuComMenosDeTresLinhas() throws Exception {
        Path vazio = temp.resolve("vazio.SIP"); Files.createFile(vazio);
        assertThrows(Exception.class, () -> new AnaliseSipatService().analisar(vazio, null));
        Path duas = temp.resolve("duas.SIP");
        new ArquivoSipatWriter().escrever(duas, List.of(TestData.linha('0', 1), TestData.linha('9', 2)));
        SipatValidationException erro = assertThrows(SipatValidationException.class, () -> new AnaliseSipatService().analisar(duas, null));
        assertTrue(erro.getMessage().contains("cabeçalho"));
    }

    @Test void rejeitaTiposDeCabecalhoDetalheERodapeInvalidos() throws Exception {
        String detalhe = TestData.detalhe("00000000000001", "00000000000001", 2, null);
        Path cabecalho = temp.resolve("cab.SIP");
        new ArquivoSipatWriter().escrever(cabecalho, List.of(TestData.linha('1', 1), detalhe, TestData.linha('9', 3)));
        assertThrows(SipatValidationException.class, () -> new AnaliseSipatService().analisar(cabecalho, null));
        Path meio = temp.resolve("meio.SIP");
        new ArquivoSipatWriter().escrever(meio, List.of(TestData.linha('0', 1), TestData.linha('2', 2), TestData.linha('9', 3)));
        assertThrows(SipatValidationException.class, () -> new AnaliseSipatService().analisar(meio, null));
        Path rodape = temp.resolve("rod.SIP");
        new ArquivoSipatWriter().escrever(rodape, List.of(TestData.linha('0', 1), detalhe, TestData.linha('0', 3)));
        assertThrows(SipatValidationException.class, () -> new AnaliseSipatService().analisar(rodape, null));
    }

    @Test void rejeitaSequencialNaoNumerico() throws Exception {
        String detalhe = TestData.detalhe("00000000000001", "00000000000001", 2, null);
        detalhe = SipatParser.substituir(detalhe, 795, 800, "00A02");
        Path arquivo = temp.resolve("seq.SIP");
        new ArquivoSipatWriter().escrever(arquivo, List.of(TestData.linha('0', 1), detalhe, TestData.linha('9', 3)));
        assertThrows(SipatValidationException.class, () -> new AnaliseSipatService().analisar(arquivo, null));
    }

    @Test void validacaoPosGravacaoDetectaControleDivergente() throws Exception {
        Path arquivo = TestData.arquivo(temp.resolve("controle.SIP"), List.of(
                TestData.detalhe("00000000000001", "00000000000001", 2, null)));
        List<String> linhas = new java.util.ArrayList<>(Files.readAllLines(arquivo, java.nio.charset.Charset.forName("windows-1252")));
        linhas.set(0, SipatParser.substituir(linhas.getFirst(), 28, 33, "99999"));
        new ArquivoSipatWriter().escrever(arquivo, linhas);
        var resultado = new ValidacaoPosGravacaoService().validar(arquivo);
        assertFalse(resultado.sucesso());
        assertTrue(resultado.itens().stream().anyMatch(i -> i.verificacao().contains("Quantidades") && !i.sucesso()));
    }
}
