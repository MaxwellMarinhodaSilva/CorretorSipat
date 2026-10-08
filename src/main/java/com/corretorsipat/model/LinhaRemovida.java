package com.corretorsipat.model;

import java.math.BigInteger;

/** Conserva as informações de auditoria de um detalhe removido por duplicidade. */
public record LinhaRemovida(
        int linhaOriginalRemovida,
        String protocolo,
        int linhaOriginalPreservada,
        BigInteger valorRemovido,
        String controleDevedor,
        String classificacao,
        String motivo
) {
    public static final String IDENTICA = "linha idêntica";
    public static final String DIFERENTE = "mesmo protocolo com outros campos diferentes";
    public static final String MOTIVO_DUPLICADO = "protocolo já encontrado anteriormente";
}
