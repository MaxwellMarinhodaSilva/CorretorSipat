package com.corretorsipat.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolve de forma centralizada a pasta-base e os diretórios auxiliares da aplicação. */
public final class DiretoriosAplicacao {
    private final Path pastaBase;

    public DiretoriosAplicacao() {
        this(Path.of(System.getProperty("user.dir")));
    }

    DiretoriosAplicacao(Path pastaBase) {
        this.pastaBase = pastaBase.toAbsolutePath().normalize();
    }

    private static void prepararPasta(Path pasta, String descricao) throws IOException {
        try {
            Files.createDirectories(pasta);
        } catch (IOException ex) {
            throw new IOException("Não foi possível criar a pasta " + descricao + ": " + pasta, ex);
        }
        if (!Files.isDirectory(pasta) || !Files.isWritable(pasta)) {
            throw new IOException("A pasta " + descricao + " não permite gravação: " + pasta);
        }
    }

    public Path pastaBase() {
        return pastaBase;
    }

    public Path arquivoHistorico() {
        return pastaBase.resolve("Historico").resolve("historico.txt");
    }

    public Path pastaLogs() {
        return pastaBase.resolve("Log");
    }

    public Path pastaRelatoriosCsv() {
        return pastaBase.resolve("Relatorios").resolve("CSV");
    }

    public Path pastaRelatoriosTxt() {
        return pastaBase.resolve("Relatorios").resolve("TXT");
    }

    public void prepararPastasDeSaida() throws IOException {
        prepararPasta(pastaLogs(), "de logs");
        prepararPasta(pastaRelatoriosCsv(), "de relatórios CSV");
        prepararPasta(pastaRelatoriosTxt(), "de relatórios TXT");
    }
}
