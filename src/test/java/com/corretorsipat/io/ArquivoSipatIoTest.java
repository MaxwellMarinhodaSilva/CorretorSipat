package com.corretorsipat.io;

import com.corretorsipat.TestData;
import com.corretorsipat.model.ArquivoSip;
import com.corretorsipat.model.TipoQuebraLinha;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Confirma preservação de bytes, Windows-1252 e CRLF no ciclo de I/O SIPAT. */
class ArquivoSipatIoTest {
    @TempDir Path temp;

    @Test void gravaWindows1252CrLfFinalSemBom() throws Exception {
        String detalhe = TestData.detalhe("00000000000001", "00000000000010", 2, "Ação cartorária");
        Path arquivo = TestData.arquivo(temp.resolve("formato.SIP"), List.of(detalhe));
        byte[] bytes = Files.readAllBytes(arquivo);
        assertFalse(bytes.length >= 3 && bytes[0] == (byte) 0xef && bytes[1] == (byte) 0xbb && bytes[2] == (byte) 0xbf);
        assertEquals("\r\n", new String(bytes, bytes.length - 2, 2, Charset.forName("windows-1252")));
        assertFalse(new String(bytes, Charset.forName("windows-1252")).replace("\r\n", "").contains("\n"));
        ArquivoSip lido = new ArquivoSipatReader().ler(arquivo);
        assertEquals("windows-1252", lido.codificacao().toLowerCase()); assertEquals(TipoQuebraLinha.CRLF, lido.quebraLinha());
        assertTrue(lido.terminaComQuebra()); assertFalse(lido.possuiBom());
        assertTrue(lido.registros().stream().allMatch(r -> r.conteudo().length() == 800));
    }
}
