package com.corretorsipat.model;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Agrega análise, correções, validação, duração e caminhos de uma correção concluída. */
public record ResultadoProcessamento(
        LocalDateTime dataHora,
        AnaliseSipat analise,
        int linhasDepois,
        int detalhesDepois,
        BigInteger somaFinal,
        String quantidadeCabecalho,
        String segurancaRodape,
        String sequencialFinal,
        int sequenciaisRenumerados,
        List<CorrecaoCampo> correcoes,
        ResultadoValidacao validacao,
        Duration duracao,
        ArquivosGerados arquivos,
        String sha256Original,
        String sha256Corrigido
) {
    public ResultadoProcessamento {
        correcoes = List.copyOf(correcoes);
    }
}
