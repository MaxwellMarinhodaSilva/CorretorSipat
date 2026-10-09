package com.corretorsipat.model;

/**
 * Resultado individual de uma verificação independente feita após a gravação.
 */
public record ItemValidacao(String verificacao, boolean sucesso, String detalhe) {
}
