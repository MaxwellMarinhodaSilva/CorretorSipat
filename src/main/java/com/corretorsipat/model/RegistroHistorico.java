package com.corretorsipat.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Representa uma operação concluída reapresentada pelo histórico local.
 */
public record RegistroHistorico(Path arquivo, LocalDateTime dataHora, String operacao, boolean sucesso) {
}
