package com.corretorsipat.model;

/**
 * Descreve uma alteração estrutural aplicada a um campo posicional do SIPAT.
 */
public record CorrecaoCampo(String campo, String valorAnterior, String valorPosterior) {
}
