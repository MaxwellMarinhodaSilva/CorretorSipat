package com.corretorsipat.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record RegistroHistorico(Path arquivo, LocalDateTime dataHora, String operacao, boolean sucesso) {
}
