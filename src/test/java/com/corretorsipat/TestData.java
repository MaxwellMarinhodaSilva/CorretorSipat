package com.corretorsipat;

import com.corretorsipat.io.ArquivoSipatWriter;
import com.corretorsipat.parser.LayoutSipat;
import com.corretorsipat.parser.SipatParser;
import com.corretorsipat.util.FormatUtil;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TestData {
    private TestData() { }

    public static String linha(char tipo, int sequencial) {
        char[] chars = new char[800]; java.util.Arrays.fill(chars, ' '); chars[0] = tipo;
        return SipatParser.substituir(new String(chars), 795, 800, FormatUtil.zeros(sequencial, 5));
    }

    public static String detalhe(String protocolo, String valor, int sequencial, String marcador) {
        String linha = linha('1', sequencial);
        if (marcador != null) {
            String m = String.format("%-20s", marcador).substring(0, 20);
            linha = SipatParser.substituir(linha, 20, 40, m);
        }
        linha = SipatParser.substituir(linha, LayoutSipat.VALOR_INICIO, LayoutSipat.VALOR_FIM, valor);
        return SipatParser.substituir(linha, LayoutSipat.PROTOCOLO_INICIO, LayoutSipat.PROTOCOLO_FIM, protocolo);
    }

    public static Path arquivo(Path path, List<String> detalhes) throws Exception {
        String qtd = FormatUtil.zeros(detalhes.size(), 5);
        BigInteger soma = detalhes.stream().map(l -> new BigInteger(l.substring(578, 592))).reduce(BigInteger.ZERO, BigInteger::add);
        String cab = linha('0', 1);
        cab = SipatParser.substituir(cab, 28, 33, qtd); cab = SipatParser.substituir(cab, 38, 43, qtd);
        String rod = linha('9', detalhes.size() + 2);
        rod = SipatParser.substituir(rod, 12, 17, FormatUtil.zeros(detalhes.size() * 2, 5));
        rod = SipatParser.substituir(rod, 17, 35, FormatUtil.zeros(soma, 18));
        List<String> linhas = new ArrayList<>(); linhas.add(cab); linhas.addAll(detalhes); linhas.add(rod);
        new ArquivoSipatWriter().escrever(path, linhas); return path;
    }
}
