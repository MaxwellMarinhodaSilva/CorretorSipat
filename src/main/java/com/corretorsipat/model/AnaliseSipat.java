package com.corretorsipat.model;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;

/** Reúne a leitura, métricas e duplicidades apuradas em uma análise SIPAT. */
public record AnaliseSipat(
        Path arquivo,
        ArquivoSip arquivoSip,
        int totalLinhas,
        int totalDetalhes,
        int protocolosUnicos,
        int gruposDuplicados,
        int ocorrenciasExcedentes,
        BigInteger somaOriginal,
        BigInteger somaCorrigida,
        String cabecalhoQuantidadeAnterior,
        String cabecalhoIndicacoesAnterior,
        String rodapeSegurancaAnterior,
        String rodapeSomaAnterior,
        int sequenciaisIncorretos,
        List<RegistroSip> detalhesPreservados,
        List<GrupoDuplicidade> duplicidades,
        List<LinhaRemovida> linhasRemovidas,
        List<Inconsistencia> inconsistencias
) {
    public AnaliseSipat {
        detalhesPreservados = List.copyOf(detalhesPreservados);
        duplicidades = List.copyOf(duplicidades);
        linhasRemovidas = List.copyOf(linhasRemovidas);
        inconsistencias = List.copyOf(inconsistencias);
    }
}
