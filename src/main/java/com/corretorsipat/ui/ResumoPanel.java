package com.corretorsipat.ui;

import com.corretorsipat.model.AnaliseSipat;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public final class ResumoPanel extends JPanel {
    private final JLabel registros = valor();
    private final JLabel unicos = valor();
    private final JLabel grupos = valor();
    private final JLabel removidos = valor();
    private final JLabel somaOriginal = valor();
    private final JLabel somaCorrigida = valor();
    private final DuplicidadesTableModel modelo = new DuplicidadesTableModel();
    private final JPanel indicadores;
    private final JTable tabela;

    public ResumoPanel() {
        super(new BorderLayout());
        indicadores = new JPanel(new GridLayout(1, 6, 8, 0));
        indicadores.add(card("Registros", registros));
        indicadores.add(card("Protocolos únicos", unicos));
        indicadores.add(card("Grupos duplicados", grupos));
        indicadores.add(card("Removidos", removidos));
        indicadores.add(card("Soma original", somaOriginal));
        indicadores.add(card("Soma corrigida", somaCorrigida));
        indicadores.setAlignmentX(LEFT_ALIGNMENT);
        indicadores.setMaximumSize(new Dimension(Integer.MAX_VALUE, indicadores.getPreferredSize().height));

        tabela = new JTable(modelo);
        tabela.setAutoCreateRowSorter(true);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        centralizarTabela();
        JScrollPane scroll = new JScrollPane(tabela,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createTitledBorder("Duplicidades encontradas"));
        add(scroll, BorderLayout.CENTER);
        ajustarColunas();
        limpar();
    }

    private static JPanel card(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        p.add(new JLabel(titulo), BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 15f));
        return l;
    }

    private void centralizarTabela() {
        TableCellRenderer cabecalhoOriginal = tabela.getTableHeader().getDefaultRenderer();
        tabela.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
            Component componente = cabecalhoOriginal.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (componente instanceof JLabel label) label.setHorizontalAlignment(SwingConstants.CENTER);
            return componente;
        });
        DefaultTableCellRenderer celula = new DefaultTableCellRenderer();
        celula.setHorizontalAlignment(SwingConstants.CENTER);
        tabela.setDefaultRenderer(String.class, celula);
        tabela.setDefaultRenderer(Integer.class, celula);
    }

    public JPanel indicadores() {
        return indicadores;
    }

    public void exibir(AnaliseSipat a) {
        registros.setText(Integer.toString(a.totalDetalhes()));
        unicos.setText(Integer.toString(a.protocolosUnicos()));
        grupos.setText(Integer.toString(a.gruposDuplicados()));
        removidos.setText(Integer.toString(a.ocorrenciasExcedentes()));
        somaOriginal.setText(a.somaOriginal().toString());
        somaCorrigida.setText(a.somaCorrigida().toString());
        modelo.setDados(a.duplicidades());
        SwingUtilities.invokeLater(this::ajustarColunas);
    }

    public void limpar() {
        for (JLabel l : new JLabel[]{registros, unicos, grupos, removidos, somaOriginal, somaCorrigida}) l.setText("—");
        modelo.setDados(java.util.List.of());
        tabela.clearSelection();
    }

    private void ajustarColunas() {
        int[] minimos = {170, 140, 190, 110};
        for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
            TableColumn tableColumn = tabela.getColumnModel().getColumn(coluna);
            int largura = larguraPreferida(coluna, minimos[coluna]);
            tableColumn.setMinWidth(largura);
            tableColumn.setPreferredWidth(largura);
        }
    }

    private int larguraPreferida(int coluna, int minimo) {
        TableCellRenderer cabecalho = tabela.getTableHeader().getDefaultRenderer();
        Component componente = cabecalho.getTableCellRendererComponent(
                tabela, tabela.getColumnName(coluna), false, false, -1, coluna);
        int largura = componente.getPreferredSize().width + 18;
        for (int linha = 0; linha < tabela.getRowCount(); linha++) {
            TableCellRenderer renderer = tabela.getCellRenderer(linha, coluna);
            componente = tabela.prepareRenderer(renderer, linha, coluna);
            largura = Math.max(largura, componente.getPreferredSize().width + 18);
        }
        return Math.max(minimo, largura);
    }
}
