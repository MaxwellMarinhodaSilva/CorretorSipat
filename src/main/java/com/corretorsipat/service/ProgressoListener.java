package com.corretorsipat.service;

@FunctionalInterface
/** Contrato desacoplado para comunicar percentual e etapa de processamento. */
public interface ProgressoListener {
    ProgressoListener NENHUM = (percentual, etapa) -> {
    };

    void atualizar(int percentual, String etapa);
}
