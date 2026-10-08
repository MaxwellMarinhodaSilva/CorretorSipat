package com.corretorsipat.ui;

import com.corretorsipat.VersaoAplicacao;
import com.corretorsipat.model.CorrecaoCampo;
import com.corretorsipat.model.ResultadoProcessamento;
import com.corretorsipat.report.ResumoTextoService;
import com.corretorsipat.util.DesktopUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;
import java.util.stream.Collectors;

public final class ProcessamentoConcluidoDialog extends JDialog {
    private final ResultadoProcessamento resultado;

    public ProcessamentoConcluidoDialog(Frame owner, ResultadoProcessamento resultado) {
        super(owner, "Processamento concluído", Dialog.ModalityType.APPLICATION_MODAL);
        this.resultado = resultado;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout(0, 8));
        JPanel conteudo = new ConteudoRolavel();
        conteudo.setLayout(new BorderLayout(0, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        conteudo.add(empilhar(criarCabecalho(), criarResumo()), BorderLayout.NORTH);
        conteudo.add(criarLinhasRemovidas(), BorderLayout.CENTER);
        conteudo.add(criarPainelInferior(), BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(conteudo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
        dimensionar(owner);
    }

    private static void centralizarTabela(JTable tabela) {
        TableCellRenderer cabecalhoOriginal = tabela.getTableHeader().getDefaultRenderer();
        tabela.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
            Component componente = cabecalhoOriginal.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (componente instanceof JLabel label) label.setHorizontalAlignment(SwingConstants.CENTER);
            return componente;
        });
        DefaultTableCellRenderer celula = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setToolTipText(value == null || value.toString().isBlank() ? null : value.toString());
                return componente;
            }
        };
        tabela.setDefaultRenderer(Object.class, celula);
        tabela.setDefaultRenderer(String.class, celula);
        tabela.setDefaultRenderer(Integer.class, celula);
        tabela.setDefaultRenderer(java.math.BigInteger.class, celula);
    }

    private static JPanel empilhar(Component primeiro, Component segundo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.add(primeiro);
        painel.add(Box.createVerticalStrut(10));
        painel.add(segundo);
        return painel;
    }

    private static JPanel colunaResumo(Object... dados) {
        JPanel coluna = new JPanel(new GridBagLayout());
        for (int linha = 0; linha < dados.length; linha += 2) {
            GridBagConstraints rotulo = new GridBagConstraints();
            rotulo.gridx = 0;
            rotulo.gridy = linha / 2;
            rotulo.weightx = 1;
            rotulo.fill = GridBagConstraints.HORIZONTAL;
            rotulo.anchor = GridBagConstraints.LINE_START;
            rotulo.insets = new Insets(0, 0, 6, 10);
            coluna.add(new JLabel(dados[linha] + ":"), rotulo);
            JLabel valor = new JLabel(String.valueOf(dados[linha + 1]));
            valor.setFont(valor.getFont().deriveFont(Font.BOLD));
            GridBagConstraints valorConstraints = new GridBagConstraints();
            valorConstraints.gridx = 1;
            valorConstraints.gridy = linha / 2;
            valorConstraints.anchor = GridBagConstraints.LINE_START;
            valorConstraints.insets = new Insets(0, 0, 6, 0);
            coluna.add(valor, valorConstraints);
        }
        return coluna;
    }

    private static JTextArea area(String texto) {
        JTextArea a = new JTextArea(texto);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setBorder(null);
        return a;
    }

    private static JTextArea areaSemQuebra(String texto) {
        JTextArea a = area(texto);
        a.setLineWrap(false);
        a.setWrapStyleWord(false);
        return a;
    }

    private static String duracao(Duration d) {
        long ms = d.toMillis();
        return ms < 1000 ? ms + " ms" : String.format("%.2f s", ms / 1000.0);
    }

    private JPanel criarCabecalho() {
        JPanel p = new JPanel(new BorderLayout(8, 3));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel titulo = new JLabel("Processamento concluído e validado");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 19f));
        JLabel indicador = new JLabel("✓ SUCESSO");
        indicador.setForeground(new Color(25, 145, 75));
        indicador.setFont(indicador.getFont().deriveFont(Font.BOLD, 14f));
        JTextArea caminho = area("Original: " + resultado.analise().arquivo() + "\nCópia: " + resultado.arquivos().sipCorrigido());
        p.add(titulo, BorderLayout.NORTH);
        p.add(indicador, BorderLayout.EAST);
        p.add(caminho, BorderLayout.CENTER);
        return p;
    }

    private JPanel criarResumo() {
        JPanel p = new JPanel(new GridLayout(1, 2, 24, 0));
        p.setBorder(BorderFactory.createTitledBorder("Resumo do processamento"));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.add(colunaResumo(
                "Linhas físicas antes / depois", resultado.analise().totalLinhas() + " / " + resultado.linhasDepois(),
                "Detalhes antes / depois", resultado.analise().totalDetalhes() + " / " + resultado.detalhesDepois(),
                "Protocolos únicos encontrados", resultado.analise().protocolosUnicos(),
                "Grupos de protocolos duplicados", resultado.analise().gruposDuplicados(),
                "Ocorrências excedentes removidas", resultado.analise().ocorrenciasExcedentes(),
                "Soma original considerada", resultado.analise().somaOriginal()));
        p.add(colunaResumo(
                "Soma final gravada no rodapé", resultado.somaFinal(),
                "Quantidade gravada no cabeçalho", resultado.quantidadeCabecalho(),
                "Somatório de segurança", resultado.segurancaRodape(),
                "Sequencial final", resultado.sequencialFinal(),
                "Validação pós-gravação", "✓ " + resultado.validacao().indicador(),
                "Duração", duracao(resultado.duracao())));
        return p;
    }

    private JPanel criarLinhasRemovidas() {
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setBorder(BorderFactory.createTitledBorder("Linhas removidas do arquivo original"));
        p.setAlignmentX(LEFT_ALIGNMENT);
        String lista = resultado.analise().linhasRemovidas().stream().map(l -> Integer.toString(l.linhaOriginalRemovida())).collect(Collectors.joining(", "));
        JTextArea compacta = area("Lista compacta: " + (lista.isBlank() ? "nenhuma" : lista));
        compacta.setFont(compacta.getFont().deriveFont(Font.BOLD));
        JTable tabela = new JTable(new LinhasRemovidasTableModel(resultado.analise().linhasRemovidas()));
        tabela.setAutoCreateRowSorter(true);
        tabela.setCellSelectionEnabled(true);
        tabela.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        centralizarTabela(tabela);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(105);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(115);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(115);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(275);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(260);
        tabela.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(tabela,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(1000, 260));
        scroll.setMinimumSize(new Dimension(0, 220));
        p.add(compacta, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel criarCorrecoes() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Correções estruturais realizadas"));
        p.setAlignmentX(LEFT_ALIGNMENT);
        StringBuilder texto = new StringBuilder();
        for (CorrecaoCampo c : resultado.correcoes())
            texto.append("• ").append(c.campo()).append(":  ").append(c.valorAnterior()).append(" → ").append(c.valorPosterior()).append('\n');
        texto.append("• Sequenciais físicos: ").append(resultado.sequenciaisRenumerados()).append(" linha(s) precisaram ser renumeradas.");
        JTextArea correcoes = area(texto.toString());
        correcoes.setRows(6);
        correcoes.setToolTipText("Correções estruturais aplicadas ao arquivo gerado.");
        p.add(correcoes, BorderLayout.CENTER);
        return p;
    }

    private JPanel criarArquivos() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Arquivos gerados"));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JTextArea caminhos = areaSemQuebra("SIP corrigido: " + resultado.arquivos().sipCorrigido()
                + "\nRelatório TXT: " + resultado.arquivos().relatorioTxt()
                + "\nRelatório CSV: " + resultado.arquivos().relatorioCsv()
                + "\nLog: " + resultado.arquivos().log()
                + "\nPasta de saída: " + resultado.arquivos().pastaBase());
        caminhos.setRows(6);
        caminhos.setToolTipText("Use a barra horizontal ou selecione o texto para consultar o caminho completo.");
        JScrollPane scroll = new JScrollPane(caminhos,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel criarPainelInferior() {
        JPanel painel = new JPanel(new GridLayout(1, 2, 10, 0));
        painel.add(criarCorrecoes());
        painel.add(criarArquivos());
        painel.setAlignmentX(LEFT_ALIGNMENT);
        return painel;
    }

    private JPanel criarRodape() {
        JPanel externo = new JPanel(new BorderLayout());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 5));
        JButton sip = botao("Abrir SIP corrigido", IconeUtil.sip(), "Abrir o arquivo SIP corrigido", () -> DesktopUtil.abrir(this, resultado.arquivos().sipCorrigido()));
        JButton txt = botao("Abrir relatório", IconeUtil.relatorio(), "Abrir o relatório detalhado em TXT", () -> DesktopUtil.abrir(this, resultado.arquivos().relatorioTxt()));
        JButton csv = botao("Abrir CSV", IconeUtil.planilha(), "Abrir a planilha CSV de linhas removidas", () -> DesktopUtil.abrir(this, resultado.arquivos().relatorioCsv()));
        JButton log = botao("Abrir log", IconeUtil.log(), "Abrir o log técnico do processamento", () -> DesktopUtil.abrir(this, resultado.arquivos().log()));
        JButton pasta = botao("Abrir pasta", IconeUtil.pasta(), "Abrir a pasta-base dos arquivos auxiliares", () -> DesktopUtil.abrir(this, resultado.arquivos().pastaBase()));
        JButton copiar = botao("Copiar resumo", IconeUtil.copiar(), "Copiar o resumo do processamento", this::copiarResumo);
        JButton fechar = botao("Fechar", IconeUtil.fechar(), "Fechar esta janela", this::dispose);
        for (JButton b : new JButton[]{sip, txt, csv, log, pasta, copiar, fechar}) botoes.add(b);
        JLabel versao = new JLabel(VersaoAplicacao.RODAPE + "  ");
        versao.setForeground(Color.GRAY);
        versao.setFont(versao.getFont().deriveFont(10f));
        externo.add(botoes, BorderLayout.CENTER);
        externo.add(versao, BorderLayout.SOUTH);
        return externo;
    }

    private JButton botao(String texto, Icon icone, String dica, Runnable acao) {
        JButton b = new JButton(texto, icone);
        b.setToolTipText(dica);
        b.setIconTextGap(7);
        b.getAccessibleContext().setAccessibleDescription(dica);
        b.addActionListener(e -> acao.run());
        return b;
    }

    private void dimensionar(Frame owner) {
        GraphicsConfiguration gc = owner == null ? getGraphicsConfiguration() : owner.getGraphicsConfiguration();
        Rectangle tela = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle util = new Rectangle(tela.x + insets.left, tela.y + insets.top,
                tela.width - insets.left - insets.right, tela.height - insets.top - insets.bottom);
        int largura = Math.min(1280, Math.max(640, (int) Math.round(util.width * 0.92)));
        int altura = Math.min(860, Math.max(560, (int) Math.round(util.height * 0.88)));
        largura = Math.min(largura, util.width);
        altura = Math.min(altura, util.height);
        if (util.width >= 980 && util.height >= 650) {
            setMinimumSize(new Dimension(980, 650));
            largura = Math.max(largura, 980);
            altura = Math.max(altura, 650);
        }
        int centroX = util.x + util.width / 2;
        int centroY = util.y + util.height / 2;
        int x = Math.max(util.x, Math.min(centroX - largura / 2, util.x + util.width - largura));
        int y = Math.max(util.y, Math.min(centroY - altura / 2, util.y + util.height - altura));
        setBounds(x, y, largura, altura);
    }

    private void copiarResumo() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(new ResumoTextoService().gerar(resultado)), null);
        javax.swing.JOptionPane.showMessageDialog(this, "Resumo copiado para a área de transferência.", "Copiar resumo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class ConteudoRolavel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 18);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
