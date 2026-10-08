package com.corretorsipat.model;

import java.nio.file.Path;
import java.util.List;

public record ArquivoSip(
        Path caminho,
        String codificacao,
        TipoQuebraLinha quebraLinha,
        boolean possuiBom,
        boolean terminaComQuebra,
        List<RegistroSip> registros
) {
    public ArquivoSip {
        registros = List.copyOf(registros);
    }
}
