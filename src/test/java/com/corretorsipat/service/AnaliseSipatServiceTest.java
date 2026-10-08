package com.corretorsipat.service;

import com.corretorsipat.TestData;
import com.corretorsipat.model.AnaliseSipat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Confirma agrupamento, ordem física e métricas calculadas na análise de duplicidades. */
class AnaliseSipatServiceTest {
    @TempDir Path temp;

    @Test void mantemPrimeiraOcorrenciaPreservaOrdemEGrupoDeDuas() throws Exception {
        Path arquivo = TestData.arquivo(temp.resolve("duas.SIP"), List.of(
                TestData.detalhe("00000000000001", "00000000000010", 2, "primeira"),
                TestData.detalhe("00000000000002", "00000000000020", 3, "segunda"),
                TestData.detalhe("00000000000001", "00000000000030", 4, "outra")));
        AnaliseSipat a = new AnaliseSipatService().analisar(arquivo, null);
        assertEquals(List.of("00000000000001", "00000000000002"), a.detalhesPreservados().stream().map(r -> r.protocolo()).toList());
        assertEquals(1, a.gruposDuplicados()); assertEquals(1, a.ocorrenciasExcedentes());
        assertEquals(4, a.linhasRemovidas().getFirst().linhaOriginalRemovida());
        assertEquals(2, a.linhasRemovidas().getFirst().linhaOriginalPreservada());
        assertEquals("mesmo protocolo com outros campos diferentes", a.linhasRemovidas().getFirst().classificacao());
        assertEquals("30", a.linhasRemovidas().getFirst().valorRemovido().toString());
    }

    @Test void grupoDeQuatroRemoveTresEUsaBigInteger() throws Exception {
        String p = "99999999999999";
        Path arquivo = TestData.arquivo(temp.resolve("quatro.SIP"), List.of(
                TestData.detalhe(p, "99999999999999", 2, "igual"),
                TestData.detalhe(p, "99999999999999", 3, "igual"),
                TestData.detalhe(p, "99999999999999", 4, "igual"),
                TestData.detalhe(p, "99999999999999", 5, "igual")));
        AnaliseSipat a = new AnaliseSipatService().analisar(arquivo, null);
        assertEquals(3, a.ocorrenciasExcedentes()); assertEquals("399999999999996", a.somaOriginal().toString());
        assertEquals("99999999999999", a.somaCorrigida().toString());
        assertTrue(a.linhasRemovidas().stream().allMatch(l -> l.classificacao().equals("linha idêntica")));
    }

    @Test void arquivoSemDuplicidadesPermaneceNaMesmaOrdem() throws Exception {
        Path arquivo = TestData.arquivo(temp.resolve("unico.SIP"), List.of(
                TestData.detalhe("00000000000003", "00000000000001", 2, null),
                TestData.detalhe("00000000000001", "00000000000002", 3, null),
                TestData.detalhe("00000000000002", "00000000000003", 4, null)));
        AnaliseSipat a = new AnaliseSipatService().analisar(arquivo, null);
        assertEquals(0, a.ocorrenciasExcedentes());
        assertEquals(List.of("00000000000003", "00000000000001", "00000000000002"), a.detalhesPreservados().stream().map(r -> r.protocolo()).toList());
    }
}
