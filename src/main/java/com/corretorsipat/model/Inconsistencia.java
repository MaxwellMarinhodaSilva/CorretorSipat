package com.corretorsipat.model;

/**
 * Registra uma divergência entre um controle esperado e o valor encontrado.
 */
public record Inconsistencia(String campo, String esperado, String encontrado) {
}
