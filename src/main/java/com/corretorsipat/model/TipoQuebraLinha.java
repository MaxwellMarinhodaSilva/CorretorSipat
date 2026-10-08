package com.corretorsipat.model;

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
