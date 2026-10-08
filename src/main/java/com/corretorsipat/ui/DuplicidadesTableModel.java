package com.corretorsipat.ui;

import com.corretorsipat.model.GrupoDuplicidade;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.stream.Collectors;

/** Adapta grupos de duplicidade para as quatro colunas da tabela principal. */
public final class DuplicidadesTableModel extends AbstractTableModel {
    private final String[] colunas = {"Protocolo", "Linha preservada", "Linhas removidas", "Ocorrências"};
    private List<GrupoDuplicidade> dados = List.of();

    public void setDados(List<GrupoDuplicidade> dados) {
        this.dados = List.copyOf(dados);
        fireTableDataChanged();
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
        return column == 1 || column == 3 ? Integer.class : String.class;
    }

    public Object getValueAt(int row, int column) {
        GrupoDuplicidade g = dados.get(row);
        return switch (column) {
            case 0 -> g.protocolo();
            case 1 -> g.linhaOriginalPreservada();
            case 2 ->
                    g.ocorrenciasRemovidas().stream().map(l -> Integer.toString(l.linhaOriginalRemovida())).collect(Collectors.joining(", "));
            case 3 -> g.ocorrenciasRemovidas().size() + 1;
            default -> "";
        };
    }
}
