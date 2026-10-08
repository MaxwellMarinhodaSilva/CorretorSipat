package com.corretorsipat.model;

import java.nio.file.Path;
import java.util.List;

/** Representa o arquivo físico lido, inclusive codificação e quebra de linha originais. */
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
