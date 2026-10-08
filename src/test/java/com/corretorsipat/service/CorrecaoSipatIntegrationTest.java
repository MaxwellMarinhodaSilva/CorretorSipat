package com.corretorsipat.service;

import com.corretorsipat.TestData;
import com.corretorsipat.io.ArquivoSipatReader;
import com.corretorsipat.model.ArquivoSip;
import com.corretorsipat.model.ResultadoProcessamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorrecaoSipatIntegrationTest {
    @TempDir Path temp;

    @Test void corrigeCamposRenumeraEValidaSemSobrescreverOriginal() throws Exception {
        Path original = TestData.arquivo(temp.resolve("entrada.SIP"), List.of(
                TestData.detalhe("00000000000001", "00000000000100", 2, "A"),
                TestData.detalhe("00000000000002", "00000000000200", 3, "B"),
                TestData.detalhe("00000000000001", "00000000000300", 4, "C")));
        byte[] antes = Files.readAllBytes(original);
        ResultadoProcessamento r = new CorrecaoSipatService().corrigir(original, null);
        assertArrayEquals(antes, Files.readAllBytes(original)); assertNotEquals(original, r.arquivos().sipCorrigido());
        assertTrue(r.validacao().sucesso()); assertEquals(2, r.detalhesDepois()); assertEquals("00002", r.quantidadeCabecalho());
        assertEquals("00004", r.segurancaRodape()); assertEquals("00004", r.sequencialFinal()); assertEquals("300", r.somaFinal().toString());
        assertTrue(Files.isRegularFile(r.arquivos().relatorioTxt())); assertTrue(Files.isRegularFile(r.arquivos().relatorioCsv())); assertTrue(Files.isRegularFile(r.arquivos().log()));
        ArquivoSip saida = new ArquivoSipatReader().ler(r.arquivos().sipCorrigido());
        assertEquals("00002", saida.registros().getFirst().conteudo().substring(28, 33));
        assertEquals("000000000000000300", saida.registros().getLast().conteudo().substring(17, 35));
        for (int i = 0; i < saida.registros().size(); i++) assertEquals(String.format("%05d", i + 1), saida.registros().get(i).sequencial());
    }

    @Test void nomesExistentesGeramVersaoNumeradaSemSubstituir() throws Exception {
        Path original = TestData.arquivo(temp.resolve("entrada.SIP"), List.of(TestData.detalhe("00000000000001", "00000000000001", 2, null)));
        ResultadoProcessamento primeiro = new CorrecaoSipatService().corrigir(original, null);
        ResultadoProcessamento segundo = new CorrecaoSipatService().corrigir(original, null);
        assertNotEquals(primeiro.arquivos().sipCorrigido(), segundo.arquivos().sipCorrigido());
        assertTrue(segundo.arquivos().sipCorrigido().getFileName().toString()
                .matches("entrada_SEM_DUPLICADOS_\\d+\\.SIP"));
    }
}
