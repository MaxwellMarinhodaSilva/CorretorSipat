package com.corretorsipat.service;

@FunctionalInterface
public interface ProgressoListener {
    ProgressoListener NENHUM = (percentual, etapa) -> {
    };

    void atualizar(int percentual, String etapa);
}
