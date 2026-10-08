package com.corretorsipat.model;

import java.math.BigInteger;

public record RegistroSip(
        int linhaOriginal,
        char tipo,
        String conteudo,
        BigInteger valor,
        String protocolo,
        String sequencial
) {
    public RegistroSip {
        if (linhaOriginal < 1) throw new IllegalArgumentException("A linha física deve ser positiva.");
        if (conteudo == null) throw new IllegalArgumentException("O conteúdo da linha é obrigatório.");
    }

    public boolean detalhe() {
        return tipo == '1';
    }
}
