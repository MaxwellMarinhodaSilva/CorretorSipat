package com.corretorsipat.ui;

import com.corretorsipat.util.DesktopUtil;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * Exibe uma falha de processamento e permite abrir seu log técnico quando disponível.
 */
public final class ErroProcessamentoDialog extends JDialog {
    public ErroProcessamentoDialog(Frame owner, String mensagem, Path log) {
        super(owner, "Falha no processamento", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        setSize(620, 270);
        setMinimumSize(new Dimension(460, 220));
        setResizable(true);
        setLocationRelativeTo(owner);
        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        JLabel titulo = new JLabel("<html><b>O arquivo não foi corrigido.</b><br>" + escapar(mensagem) + "</html>", IconeUtil.erro(), SwingConstants.LEFT);
        titulo.setIconTextGap(8);
        conteudo.add(titulo, BorderLayout.NORTH);
        JTextArea caminho = new JTextArea(log == null ? "Nenhum log pôde ser criado." : "Log técnico:\n" + log);
        caminho.setEditable(false);
        caminho.setLineWrap(true);
        caminho.setWrapStyleWord(true);
        caminho.setOpaque(false);
        conteudo.add(caminho, BorderLayout.CENTER);
        add(conteudo, BorderLayout.CENTER);
        JButton abrir = new JButton("Abrir log", IconeUtil.log());
        abrir.setEnabled(log != null);
        configurarBotao(abrir, "Abrir o log técnico desta falha");
        abrir.addActionListener(e -> DesktopUtil.abrir(this, log));
        JButton fechar = new JButton("Fechar", IconeUtil.fechar());
        configurarBotao(fechar, "Fechar esta janela");
        fechar.addActionListener(e -> dispose());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(abrir);
        botoes.add(fechar);
        add(botoes, BorderLayout.SOUTH);
    }

    private static String escapar(String s) {
        return (s == null ? "Erro não especificado." : s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void configurarBotao(JButton botao, String dica) {
        botao.setIconTextGap(7);
        botao.setToolTipText(dica);
        botao.getAccessibleContext().setAccessibleName(botao.getText());
        botao.getAccessibleContext().setAccessibleDescription(dica);
    }
}
