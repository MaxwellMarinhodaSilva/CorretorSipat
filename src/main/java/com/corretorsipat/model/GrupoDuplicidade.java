package com.corretorsipat.model;

import java.util.List;

public record GrupoDuplicidade(
        String protocolo,
        int linhaOriginalPreservada,
        List<LinhaRemovida> ocorrenciasRemovidas
) {
    public GrupoDuplicidade {
        ocorrenciasRemovidas = List.copyOf(ocorrenciasRemovidas);
    }

    public List<Integer> todasAsLinhas() {
        return java.util.stream.Stream.concat(
                java.util.stream.Stream.of(linhaOriginalPreservada),
                ocorrenciasRemovidas.stream().map(LinhaRemovida::linhaOriginalRemovida)
        ).toList();
    }
}
