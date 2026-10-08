package com.corretorsipat.io;

import com.corretorsipat.model.ArquivosGerados;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica diretórios auxiliares e nomes alternativos que evitam sobrescrita. */
class PlanoArquivosSaidaTest {
    @TempDir Path temp;

    @Test void organizaAuxiliaresNaPastaBaseEMantemSipAoLadoDaOrigem() throws Exception {
        Path base = temp.resolve("aplicacao");
        Path entrada = temp.resolve("entrada").resolve("arquivo.SIP");
        Files.createDirectories(entrada.getParent());
        Files.createFile(entrada);

        ArquivosGerados arquivos = new PlanoArquivosSaida(new DiretoriosAplicacao(base)).criar(entrada);

        assertEquals(entrada.getParent().resolve("arquivo_SEM_DUPLICADOS.SIP").toAbsolutePath(), arquivos.sipCorrigido());
        assertEquals(base.resolve("Relatorios/TXT/arquivo_RELATORIO.txt").toAbsolutePath(), arquivos.relatorioTxt());
        assertEquals(base.resolve("Relatorios/CSV/arquivo_LINHAS_REMOVIDAS.csv").toAbsolutePath(), arquivos.relatorioCsv());
        assertEquals(base.resolve("Log/arquivo_PROCESSAMENTO.log").toAbsolutePath(), arquivos.log());
        assertEquals(base.toAbsolutePath(), arquivos.pastaBase());
        assertTrue(Files.isDirectory(base.resolve("Log")));
        assertTrue(Files.isDirectory(base.resolve("Relatorios/CSV")));
        assertTrue(Files.isDirectory(base.resolve("Relatorios/TXT")));
    }

    @Test void colisaoEmQualquerDestinoUsaMesmoSufixoNumeradoSemSobrescrever() throws Exception {
        Path base = temp.resolve("aplicacao");
        Path entrada = temp.resolve("entrada").resolve("arquivo.SIP");
        Files.createDirectories(entrada.getParent());
        Files.createFile(entrada);
        DiretoriosAplicacao diretorios = new DiretoriosAplicacao(base);
        PlanoArquivosSaida plano = new PlanoArquivosSaida(diretorios);
        ArquivosGerados primeiro = plano.criar(entrada);
        Files.createFile(primeiro.log());

        ArquivosGerados segundo = plano.criar(entrada);

        assertEquals("arquivo_SEM_DUPLICADOS_2.SIP", segundo.sipCorrigido().getFileName().toString());
        assertEquals("arquivo_RELATORIO_2.txt", segundo.relatorioTxt().getFileName().toString());
        assertEquals("arquivo_LINHAS_REMOVIDAS_2.csv", segundo.relatorioCsv().getFileName().toString());
        assertEquals("arquivo_PROCESSAMENTO_2.log", segundo.log().getFileName().toString());
    }
}
