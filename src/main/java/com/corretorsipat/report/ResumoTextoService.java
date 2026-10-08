package com.corretorsipat.report;

import com.corretorsipat.model.ResultadoProcessamento;

import java.util.stream.Collectors;

/** Converte o resultado processado em um resumo textual apropriado para cópia. */
public final class ResumoTextoService {
    public String gerar(ResultadoProcessamento r) {
        String linhas = r.analise().linhasRemovidas().stream()
                .map(item -> Integer.toString(item.linhaOriginalRemovida()))
                .collect(Collectors.joining(", "));
        return "Corretor SIPAT - Processamento concluído\n"
                + "Original: " + r.analise().arquivo() + "\n"
                + "Corrigido: " + r.arquivos().sipCorrigido() + "\n"
                + "Linhas: " + r.analise().totalLinhas() + " → " + r.linhasDepois() + "\n"
                + "Detalhes: " + r.analise().totalDetalhes() + " → " + r.detalhesDepois() + "\n"
                + "Protocolos únicos: " + r.analise().protocolosUnicos() + "\n"
                + "Grupos duplicados: " + r.analise().gruposDuplicados() + "\n"
                + "Ocorrências removidas: " + r.analise().ocorrenciasExcedentes() + "\n"
                + "Soma original: " + r.analise().somaOriginal() + "\n"
                + "Soma final: " + r.somaFinal() + "\n"
                + "Cabeçalho: " + r.quantidadeCabecalho() + " | Segurança: " + r.segurancaRodape() + "\n"
                + "Sequencial final: " + r.sequencialFinal() + "\n"
                + "Validação: " + r.validacao().indicador() + "\n"
                + "Linhas removidas do arquivo original: " + (linhas.isEmpty() ? "nenhuma" : linhas) + "\n";
    }
}
