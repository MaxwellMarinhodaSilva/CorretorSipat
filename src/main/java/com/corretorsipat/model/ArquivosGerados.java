package com.corretorsipat.model;

import java.nio.file.Path;

public record ArquivosGerados(Path sipCorrigido, Path relatorioTxt, Path relatorioCsv, Path log) {
    public Path pastaBase() {
        Path pastaLog = log.toAbsolutePath().normalize().getParent();
        return pastaLog == null || pastaLog.getParent() == null ? pastaLog : pastaLog.getParent();
    }
}
