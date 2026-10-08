package com.corretorsipat.parser;

import com.corretorsipat.TestData;
import com.corretorsipat.model.RegistroSip;
import com.corretorsipat.service.SipatValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Exercita extração e substituição dos campos fixos previstos pelo layout. */
class SipatParserTest {
    @Test void extraiValorProtocoloESequencialNosLimitesExatos() throws Exception {
        String linha = TestData.detalhe("20250400136053", "00000000010621", 2, "A");
        RegistroSip r = SipatParser.parse(linha, 2);
        assertEquals("20250400136053", r.protocolo());
        assertEquals("10621", r.valor().toString());
        assertEquals("00002", r.sequencial());
        assertEquals(' ', linha.charAt(606), "A coluna física 607 não integra o protocolo");
    }

    @Test void substituicaoSeguraPreservaOitocentosCaracteres() {
        String linha = TestData.linha('0', 1);
        String alterada = SipatParser.substituir(linha, 28, 33, "00204");
        assertEquals(800, alterada.length()); assertEquals("00204", alterada.substring(28, 33));
        assertThrows(IllegalArgumentException.class, () -> SipatParser.substituir(linha, 28, 33, "204"));
    }

    @Test void rejeitaLinhaCurtaLongaValorEProtocoloInvalidos() {
        assertThrows(SipatValidationException.class, () -> SipatParser.parse("1".repeat(799), 1));
        assertThrows(SipatValidationException.class, () -> SipatParser.parse("1".repeat(801), 1));
        String valor = TestData.detalhe("20250400136053", "00000000010621", 2, null);
        valor = SipatParser.substituir(valor, 578, 592, "00000000010A21");
        String protocolo = TestData.detalhe("20250400136053", "00000000010621", 2, null);
        protocolo = SipatParser.substituir(protocolo, 592, 606, "2025040013605 ");
        String finalValor = valor, finalProtocolo = protocolo;
        assertThrows(SipatValidationException.class, () -> SipatParser.parse(finalValor, 2));
        assertThrows(SipatValidationException.class, () -> SipatParser.parse(finalProtocolo, 2));
    }
}
