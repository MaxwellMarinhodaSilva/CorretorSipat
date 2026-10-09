package com.corretorsipat.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registra detalhes técnicos de uma execução em arquivo próprio, incluindo falhas.
 */
public final class ProcessamentoLog implements AutoCloseable {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    private final Path arquivo;
    private final BufferedWriter writer;

    public ProcessamentoLog(Path arquivo) throws IOException {
        this.arquivo = arquivo;
        this.writer = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        escrever("Corretor SIPAT - log de processamento");
        escrever("Java: " + System.getProperty("java.version") + " | Sistema: " + System.getProperty("os.name"));
    }

    public Path arquivo() {
        return arquivo;
    }

    public synchronized void escrever(String mensagem) throws IOException {
        writer.write(LocalDateTime.now().format(DATA) + " - " + mensagem);
        writer.newLine();
        writer.flush();
    }

    public synchronized void erro(Throwable erro) {
        try {
            escrever("ERRO: " + erro.getMessage());
            PrintWriter print = new PrintWriter(writer);
            erro.printStackTrace(print);
            print.flush();
            writer.flush();
        } catch (IOException falhaNoLog) {
            System.err.println("Falha adicional ao registrar erro no log: " + falhaNoLog.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
