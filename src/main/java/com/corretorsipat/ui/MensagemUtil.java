package com.corretorsipat.ui;

import javax.swing.*;
import java.awt.*;

/** Padroniza diálogos informativos, de aviso e de erro exibidos ao usuário. */
public final class MensagemUtil {
    private MensagemUtil() {
    }

    public static void erro(Component parent, String mensagem) {
        JOptionPane.showMessageDialog(parent, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void aviso(Component parent, String mensagem) {
        JOptionPane.showMessageDialog(parent, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
