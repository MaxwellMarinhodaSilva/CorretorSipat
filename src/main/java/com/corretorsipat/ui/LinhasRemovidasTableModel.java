package com.corretorsipat.ui;

import com.corretorsipat.model.LinhaRemovida;

import javax.swing.table.AbstractTableModel;
import java.math.BigInteger;
import java.util.List;

/**
 * Expõe a auditoria de linhas removidas na tabela do diálogo de conclusão.
 */
public final class LinhasRemovidasTableModel extends AbstractTableModel {
    private final String[] colunas = {"Linha removida", "Protocolo", "Linha preservada", "Valor", "Controle do devedor", "Classificação", "Motivo"};
    private final List<LinhaRemovida> dados;

    public LinhasRemovidasTableModel(List<LinhaRemovida> dados) {
        this.dados = List.copyOf(dados);
    }

    public int getRowCount() {
        return dados.size();
    }

    public int getColumnCount() {
        return colunas.length;
    }

    public String getColumnName(int column) {
        return colunas[column];
    }

    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 0, 2 -> Integer.class;
            case 3 -> BigInteger.class;
            default -> String.class;
        };
    }

    public Object getValueAt(int row, int column) {
        LinhaRemovida l = dados.get(row);
        return switch (column) {
            case 0 -> l.linhaOriginalRemovida();
            case 1 -> l.protocolo();
            case 2 -> l.linhaOriginalPreservada();
            case 3 -> l.valorRemovido();
            case 4 -> l.controleDevedor();
            case 5 -> l.classificacao();
            case 6 -> l.motivo();
            default -> "";
        };
    }
}
