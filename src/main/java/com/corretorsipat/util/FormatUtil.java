package com.corretorsipat.util;

import java.math.BigInteger;

/** Reúne formatações visuais e posicionais reutilizadas nos relatórios e na interface. */
public final class FormatUtil {
    private FormatUtil() {
    }

    public static String zeros(int valor, int largura) {
        return zeros(BigInteger.valueOf(valor), largura);
    }

    public static String zeros(BigInteger valor, int largura) {
        if (valor.signum() < 0) throw new IllegalArgumentException("O valor não pode ser negativo.");
        String texto = valor.toString();
        if (texto.length() > largura) throw new IllegalArgumentException("O valor excede " + largura + " dígitos.");
        return "0".repeat(largura - texto.length()) + texto;
    }
}
