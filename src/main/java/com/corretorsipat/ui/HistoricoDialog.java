package com.corretorsipat.ui;

import com.corretorsipat.history.HistoricoService;
import com.corretorsipat.model.RegistroHistorico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Permite consultar, selecionar e limpar registros persistidos do histórico local.
 */
public final class HistoricoDialog extends JDialog {
    private static final String CARTAO_LISTA = "lista";
    private static final String CARTAO_VAZIO = "vazio";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HistoricoService service;
    private final DefaultListModel<RegistroHistorico> modelo = new DefaultListModel<>();
    private final CardLayout layoutCentral = new CardLayout();
    private final JPanel painelCentral = new JPanel(layoutCentral);
    private final JList<RegistroHistorico> lista = new JList<>(modelo);
    private JButton selecionar;
    private JButton detalhes;
    private JButton excluir;
    private JButton limpar;
    private Path selecionado;

    private HistoricoDialog(Frame owner, HistoricoService service) {
        super(owner, "Histórico de arquivos", Dialog.ModalityType.APPLICATION_MODAL);
        this.service = service;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        configurarLista();
        criarConteudoCentral();
        add(painelCentral, BorderLayout.CENTER);
        JPanel rodape = criarRodape();
        add(rodape, BorderLayout.SOUTH);
        configurarAtalhos();
        service.carregar().forEach(modelo::addElement);
        atualizarEstado();
        setSize(820, 460);
        setMinimumSize(new Dimension(Math.max(600, rodape.getPreferredSize().width + 20), 340));
        setLocationRelativeTo(owner);
    }

    public static Path selecionar(Frame owner, HistoricoService service) {
        HistoricoDialog dialog = new HistoricoDialog(owner, service);
        dialog.setVisible(true);
        return dialog.selecionado;
    }

    private void configurarLista() {
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> componente, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(componente, value, index, selected, focus);
                RegistroHistorico item = (RegistroHistorico) value;
                setText(item.arquivo().getFileName() + "  •  " + item.operacao() + "  •  " + DATA.format(item.dataHora()));
                setToolTipText(item.arquivo().toString());
                return this;
            }
        });
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) atualizarEstado();
        });
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) escolher();
            }
        });
    }

    private void criarConteudoCentral() {
        JScrollPane scroll = new JScrollPane(lista,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        painelCentral.add(scroll, CARTAO_LISTA);
        painelCentral.add(criarEstadoVazio(), CARTAO_VAZIO);
    }

    private JPanel criarEstadoVazio() {
        JPanel painel = new JPanel(new GridBagLayout());
        JPanel mensagem = new JPanel();
        mensagem.setLayout(new javax.swing.BoxLayout(mensagem, javax.swing.BoxLayout.Y_AXIS));
        mensagem.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel icone = new JLabel(IconeUtil.historico());
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);
        icone.setBorder(BorderFactory.createEmptyBorder(0, 0, 9, 0));
        JLabel titulo = new JLabel("Não existem arquivos no histórico.");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 17f));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel descricao = new JLabel("Os arquivos processados aparecerão aqui.");
        descricao.setFont(descricao.getFont().deriveFont(Font.PLAIN, 13f));
        descricao.setAlignmentX(Component.CENTER_ALIGNMENT);
        mensagem.add(icone);
        mensagem.add(titulo);
        mensagem.add(javax.swing.Box.createVerticalStrut(7));
        mensagem.add(descricao);
        painel.add(mensagem);
        return painel;
    }

    private JPanel criarRodape() {
        selecionar = botao("Selecionar", IconeUtil.abrir(), "Selecionar o arquivo deste registro", this::escolher);
        detalhes = botao("Detalhes", IconeUtil.detalhes(), "Visualizar os detalhes do registro selecionado", this::mostrarDetalhes);
        excluir = botao("Excluir", IconeUtil.excluir(), "Excluir somente o registro selecionado do histórico", this::excluirSelecionado);
        limpar = botao("Limpar histórico", IconeUtil.limparHistorico(), "Apagar todos os registros do histórico", this::limparHistorico);
        JButton fechar = botao("Fechar", IconeUtil.fechar(), "Fechar a janela de histórico", this::dispose);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botoes.add(limpar);
        botoes.add(excluir);
        botoes.add(detalhes);
        botoes.add(selecionar);
        botoes.add(fechar);
        return botoes;
    }

    private JButton botao(String texto, javax.swing.Icon icone, String dica, Runnable acao) {
        JButton botao = new JButton(texto, icone);
        botao.setIconTextGap(7);
        botao.setToolTipText(dica);
        botao.getAccessibleContext().setAccessibleName(texto);
        botao.getAccessibleContext().setAccessibleDescription(dica);
        botao.addActionListener(e -> acao.run());
        return botao;
    }

    private void atualizarEstado() {
        boolean possuiRegistros = !modelo.isEmpty();
        boolean possuiSelecao = possuiRegistros && lista.getSelectedIndex() >= 0;
        layoutCentral.show(painelCentral, possuiRegistros ? CARTAO_LISTA : CARTAO_VAZIO);
        selecionar.setEnabled(possuiSelecao);
        detalhes.setEnabled(possuiSelecao);
        excluir.setEnabled(possuiSelecao);
        limpar.setEnabled(possuiRegistros);
    }

    private void escolher() {
        RegistroHistorico item = lista.getSelectedValue();
        if (item == null) return;
        selecionado = item.arquivo();
        dispose();
    }

    private void mostrarDetalhes() {
        RegistroHistorico item = lista.getSelectedValue();
        if (item == null) return;
        JTextArea texto = new JTextArea("Arquivo: " + item.arquivo()
                + "\nData e hora: " + DATA.format(item.dataHora())
                + "\nOperação: " + item.operacao()
                + "\nResultado: " + (item.sucesso() ? "Sucesso" : "Falha"));
        texto.setEditable(false);
        texto.setOpaque(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setColumns(52);
        texto.setRows(5);
        JOptionPane.showMessageDialog(this, texto, "Detalhes do histórico", JOptionPane.INFORMATION_MESSAGE,
                IconeUtil.detalhes());
    }

    private void excluirSelecionado() {
        RegistroHistorico item = lista.getSelectedValue();
        if (item == null) return;
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja excluir somente o registro selecionado do histórico?\n\n" + item.arquivo().getFileName(),
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) return;
        try {
            service.remover(item);
            modelo.removeElement(item);
            lista.clearSelection();
            atualizarEstado();
        } catch (Exception ex) {
            MensagemUtil.erro(this, "Não foi possível excluir o registro selecionado do histórico.");
        }
    }

    private void limparHistorico() {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Tem certeza de que deseja apagar todo o histórico?\n\nEsta ação não poderá ser desfeita.",
                "Apagar todo o histórico", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) return;
        try {
            service.limpar();
            modelo.clear();
            atualizarEstado();
        } catch (Exception ex) {
            MensagemUtil.erro(this, "Não foi possível limpar o histórico.");
        }
    }

    private void configurarAtalhos() {
        lista.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "selecionarHistorico");
        lista.getActionMap().put("selecionarHistorico", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                escolher();
            }
        });
        lista.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "excluirHistorico");
        lista.getActionMap().put("excluirHistorico", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirSelecionado();
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "fecharHistorico");
        getRootPane().getActionMap().put("fecharHistorico", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
