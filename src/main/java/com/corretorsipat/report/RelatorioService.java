package com.corretorsipat.report;

import com.corretorsipat.io.ArquivoTextoUtil;
import com.corretorsipat.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Gera os relatórios TXT e CSV a partir do resultado imutável do processamento. */
public final class RelatorioService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static String csv(String valor) {
        String texto = valor == null ? "" : valor;
        return texto.indexOf(';') >= 0 || texto.indexOf('"') >= 0 || texto.indexOf('\n') >= 0
                ? '"' + texto.replace("\"", "\"\"") + '"' : texto;
    }

    public void salvar(ResultadoProcessamento resultado) throws IOException {
        ArquivoTextoUtil.escreverAtomico(resultado.arquivos().relatorioTxt(), gerarTxt(resultado), StandardCharsets.UTF_8);
        ArquivoTextoUtil.escreverAtomico(resultado.arquivos().relatorioCsv(), gerarCsv(resultado), StandardCharsets.UTF_8);
    }

    public String gerarTxt(ResultadoProcessamento r) {
        StringBuilder s = new StringBuilder();
        s.append("CORRETOR SIPAT - RELATÓRIO DETALHADO\n");
        s.append("Data e hora: ").append(r.dataHora().format(DATA)).append('\n');
        s.append("Arquivo de entrada: ").append(r.analise().arquivo()).append('\n');
        s.append("Arquivo de saída: ").append(r.arquivos().sipCorrigido()).append('\n');
        s.append("Codificação detectada: ").append(r.analise().arquivoSip().codificacao()).append('\n');
        s.append("Quebra de linha detectada: ").append(r.analise().arquivoSip().quebraLinha().descricao()).append('\n');
        s.append("Linhas físicas antes/depois: ").append(r.analise().totalLinhas()).append(" / ").append(r.linhasDepois()).append('\n');
        s.append("Detalhes antes/depois: ").append(r.analise().totalDetalhes()).append(" / ").append(r.detalhesDepois()).append('\n');
        s.append("Protocolos únicos: ").append(r.analise().protocolosUnicos()).append('\n');
        s.append("Grupos duplicados: ").append(r.analise().gruposDuplicados()).append('\n');
        s.append("Ocorrências excedentes: ").append(r.analise().ocorrenciasExcedentes()).append('\n');
        s.append("Soma antes/depois: ").append(r.analise().somaOriginal()).append(" / ").append(r.somaFinal()).append("\n\n");

        s.append("CORREÇÕES ESTRUTURAIS\n");
        for (CorrecaoCampo c : r.correcoes())
            s.append("- ").append(c.campo()).append(": ").append(c.valorAnterior()).append(" → ").append(c.valorPosterior()).append('\n');
        s.append("- Sequenciais físicos renumerados: ").append(r.sequenciaisRenumerados()).append("\n\n");

        s.append("PROTOCOLOS DUPLICADOS\n");
        if (r.analise().duplicidades().isEmpty()) s.append("Nenhum.\n");
        for (GrupoDuplicidade g : r.analise().duplicidades())
            s.append("- ").append(g.protocolo()).append(" | linhas ").append(g.todasAsLinhas()).append('\n');
        s.append('\n');

        s.append("REMOÇÕES\n");
        for (LinhaRemovida l : r.analise().linhasRemovidas()) {
            s.append("- Protocolo ").append(l.protocolo()).append(" | linha removida ").append(l.linhaOriginalRemovida())
                    .append(" | linha preservada ").append(l.linhaOriginalPreservada()).append(" | valor ").append(l.valorRemovido())
                    .append(" | ").append(l.classificacao()).append(" | ").append(l.motivo()).append('\n');
        }
        String compacta = r.analise().linhasRemovidas().stream().map(l -> Integer.toString(l.linhaOriginalRemovida())).collect(Collectors.joining(", "));
        s.append("Lista compacta de linhas removidas: ").append(compacta.isEmpty() ? "nenhuma" : compacta).append("\n\n");

        s.append("VALIDAÇÃO PÓS-GRAVAÇÃO\n");
        for (ItemValidacao item : r.validacao().itens())
            s.append("- [").append(item.sucesso() ? "OK" : "ERRO").append("] ").append(item.verificacao()).append(": ").append(item.detalhe()).append('\n');
        s.append("Resultado consolidado: ").append(r.validacao().indicador()).append("\n\n");
        s.append("SHA-256 original: ").append(r.sha256Original()).append('\n');
        s.append("SHA-256 corrigido: ").append(r.sha256Corrigido()).append('\n');
        s.append("Duração: ").append(r.duracao().toMillis()).append(" ms\n");
        return s.toString();
    }

    public String gerarCsv(ResultadoProcessamento r) {
        StringBuilder s = new StringBuilder("linha_removida;protocolo;linha_preservada;valor;controle_devedor;classificacao;motivo\r\n");
        for (LinhaRemovida l : r.analise().linhasRemovidas()) {
            s.append(l.linhaOriginalRemovida()).append(';').append(csv(l.protocolo())).append(';')
                    .append(l.linhaOriginalPreservada()).append(';').append(l.valorRemovido()).append(';')
                    .append(csv(l.controleDevedor())).append(';').append(csv(l.classificacao())).append(';')
                    .append(csv(l.motivo())).append("\r\n");
        }
        return s.toString();
    }
}
