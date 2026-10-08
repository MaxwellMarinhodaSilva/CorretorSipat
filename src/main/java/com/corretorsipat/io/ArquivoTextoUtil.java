package com.corretorsipat.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Oferece escrita textual atômica para relatórios, histórico, configurações e logs. */
public final class ArquivoTextoUtil {
    private ArquivoTextoUtil() {
    }

    public static void escreverAtomico(Path destino, String conteudo, Charset charset) throws IOException {
        Path absoluto = destino.toAbsolutePath();
        Path pasta = absoluto.getParent();
        if (pasta == null) throw new IOException("Não foi possível determinar a pasta de saída.");
        Files.createDirectories(pasta);
        if (!Files.isWritable(pasta)) throw new IOException("A pasta não permite gravação: " + pasta);
        Path temporario = Files.createTempFile(pasta, destino.getFileName().toString() + ".", ".tmp");
        try {
            Files.writeString(temporario, conteudo, charset);
            moverAtomico(temporario, absoluto);
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    public static void moverAtomico(Path temporario, Path destino) throws IOException {
        try {
            Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporario, destino);
        }
    }
}
