package com.corretorsipat.model;

import java.util.List;

/** Consolida os itens verificados depois que o SIP corrigido é gravado. */
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
