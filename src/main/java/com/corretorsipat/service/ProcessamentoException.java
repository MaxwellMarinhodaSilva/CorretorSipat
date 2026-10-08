package com.corretorsipat.service;

import java.nio.file.Path;

/** Exceção com contexto de log para falhas ocorridas durante a correção. */
public class ProcessamentoException extends Exception {
    private final Path arquivoLog;

    public ProcessamentoException(String message, Path arquivoLog, Throwable cause) {
        super(message, cause);
        this.arquivoLog = arquivoLog;
    }

    public Path arquivoLog() {
        return arquivoLog;
    }
}
