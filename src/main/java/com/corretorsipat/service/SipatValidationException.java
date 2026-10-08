package com.corretorsipat.service;

public class SipatValidationException extends Exception {
    public SipatValidationException(String message) {
        super(message);
    }

    public SipatValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
