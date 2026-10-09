package com.corretorsipat.model;

/**
 * Enumera as convenções de quebra de linha reconhecidas durante a leitura.
 */
public enum TipoQuebraLinha {
    CRLF("CRLF"), LF("LF"), CR("CR"), MISTA("mista"), AUSENTE("ausente");

    private final String descricao;

    TipoQuebraLinha(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
