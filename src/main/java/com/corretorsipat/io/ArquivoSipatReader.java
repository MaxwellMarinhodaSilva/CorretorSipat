package com.corretorsipat.io;

import com.corretorsipat.model.ArquivoSip;
import com.corretorsipat.model.RegistroSip;
import com.corretorsipat.model.TipoQuebraLinha;
import com.corretorsipat.parser.SipatParser;
import com.corretorsipat.service.SipatValidationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ArquivoSipatReader {
    public static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final long LIMITE_BYTES = 200L * 1024 * 1024;

    private static void validarLeitura(Path origem) throws IOException {
        if (origem == null || !Files.isRegularFile(origem)) {
            throw new IOException("O arquivo selecionado não existe ou não é um arquivo válido.");
        }
        if (!Files.isReadable(origem)) throw new IOException("O arquivo selecionado não permite leitura.");
        long tamanho = Files.size(origem);
        if (tamanho == 0) throw new IOException("O arquivo está vazio.");
        if (tamanho > LIMITE_BYTES) throw new IOException("O arquivo excede o limite seguro de 200 MB.");
    }

    private static Decodificacao decodificar(byte[] bytes, boolean bom) throws IOException {
        if (bom) {
            if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xef && (bytes[1] & 0xff) == 0xbb && (bytes[2] & 0xff) == 0xbf) {
                return new Decodificacao(decodificarEstrito(Arrays.copyOfRange(bytes, 3, bytes.length), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
            throw new IOException("A codificação com BOM encontrada não é suportada.");
        }
        try {
            return new Decodificacao(decodificarEstrito(bytes, WINDOWS_1252), WINDOWS_1252);
        } catch (CharacterCodingException ex) {
            try {
                return new Decodificacao(decodificarEstrito(bytes, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            } catch (CharacterCodingException utf8) {
                throw new IOException("Não foi possível decodificar o arquivo com segurança.", utf8);
            }
        }
    }

    private static String decodificarEstrito(byte[] bytes, Charset charset) throws CharacterCodingException {
        return charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString();
    }

    private static boolean possuiBom(byte[] bytes) {
        return bytes.length >= 3 && (bytes[0] & 0xff) == 0xef && (bytes[1] & 0xff) == 0xbb && (bytes[2] & 0xff) == 0xbf
                || bytes.length >= 2 && (((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xfe)
                || ((bytes[0] & 0xff) == 0xfe && (bytes[1] & 0xff) == 0xff));
    }

    private static TipoQuebraLinha detectarQuebra(String texto) {
        int crlf = contar(texto, "\r\n");
        String semCrLf = texto.replace("\r\n", "");
        int lf = contar(semCrLf, "\n");
        int cr = contar(semCrLf, "\r");
        int tipos = (crlf > 0 ? 1 : 0) + (lf > 0 ? 1 : 0) + (cr > 0 ? 1 : 0);
        if (tipos == 0) return TipoQuebraLinha.AUSENTE;
        if (tipos > 1) return TipoQuebraLinha.MISTA;
        if (crlf > 0) return TipoQuebraLinha.CRLF;
        return lf > 0 ? TipoQuebraLinha.LF : TipoQuebraLinha.CR;
    }

    private static int contar(String texto, String alvo) {
        int total = 0;
        for (int pos = 0; (pos = texto.indexOf(alvo, pos)) >= 0; pos += alvo.length()) total++;
        return total;
    }

    public ArquivoSip ler(Path origem) throws IOException, SipatValidationException {
        validarLeitura(origem);
        byte[] bytes = Files.readAllBytes(origem);
        boolean bom = possuiBom(bytes);
        Decodificacao decodificacao = decodificar(bytes, bom);
        String texto = decodificacao.texto();
        TipoQuebraLinha quebra = detectarQuebra(texto);
        boolean terminaComQuebra = texto.endsWith("\r\n") || texto.endsWith("\n") || texto.endsWith("\r");
        String[] partes = texto.split("\r\n|\n|\r", -1);
        int limite = terminaComQuebra ? partes.length - 1 : partes.length;

        List<RegistroSip> registros = new ArrayList<>(Math.max(limite, 0));
        for (int i = 0; i < limite; i++) {
            registros.add(SipatParser.parse(partes[i], i + 1));
        }
        return new ArquivoSip(
                origem.toAbsolutePath().normalize(),
                decodificacao.charset().displayName(),
                quebra,
                bom,
                terminaComQuebra,
                registros
        );
    }

    private record Decodificacao(String texto, Charset charset) {
    }
}
