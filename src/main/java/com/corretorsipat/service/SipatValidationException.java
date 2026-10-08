package com.corretorsipat.service;

/** Indica que a entrada ou saída não atende à estrutura obrigatória do SIPAT. */
public class SipatValidationException extends Exception {
    public SipatValidationException(String message) {
        super(message);
    }

    public SipatValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
