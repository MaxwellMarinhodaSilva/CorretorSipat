package com.corretorsipat.model;

import java.util.List;

public record ResultadoValidacao(List<ItemValidacao> itens) {
    public ResultadoValidacao {
        itens = List.copyOf(itens);
    }

    public boolean sucesso() {
        return itens.stream().allMatch(ItemValidacao::sucesso);
    }

    public String indicador() {
        return sucesso() ? "SUCESSO" : "ERRO";
    }
}
