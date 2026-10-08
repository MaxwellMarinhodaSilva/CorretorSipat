package com.corretorsipat.io;

import com.corretorsipat.parser.LayoutSipat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ArquivoSipatWriter {
    public void escrever(Path destino, List<String> linhas) throws IOException {
        if (linhas == null || linhas.isEmpty()) throw new IOException("Não há registros para gravar.");
        for (int i = 0; i < linhas.size(); i++) {
            if (linhas.get(i).length() != LayoutSipat.TAMANHO_REGISTRO) {
                throw new IOException("A linha de saída " + (i + 1) + " não possui 800 caracteres.");
            }
        }
        String conteudo = String.join("\r\n", linhas) + "\r\n";
        byte[] bytes;
        try {
            ByteBuffer buffer = ArquivoSipatReader.WINDOWS_1252.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(conteudo));
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        } catch (Exception ex) {
            throw new IOException("O conteúdo possui caractere incompatível com Windows-1252.", ex);
        }

        Path absoluto = destino.toAbsolutePath();
        Path pasta = absoluto.getParent();
        if (pasta == null || !Files.isDirectory(pasta) || !Files.isWritable(pasta)) {
            throw new IOException("A pasta de saída não existe ou não permite escrita.");
        }
        Path temporario = Files.createTempFile(pasta, destino.getFileName().toString() + ".", ".tmp");
        try {
            Files.write(temporario, bytes);
            ArquivoTextoUtil.moverAtomico(temporario, absoluto);
        } finally {
            Files.deleteIfExists(temporario);
        }
    }
}
