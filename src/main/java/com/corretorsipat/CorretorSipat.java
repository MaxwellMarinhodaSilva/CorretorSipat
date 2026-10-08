package com.corretorsipat;

import com.corretorsipat.theme.WindowsTheme;
import com.corretorsipat.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Inicializa o tema e agenda a criação da janela principal na EDT do Swing. */
public final class CorretorSipat {
    private CorretorSipat() { }

    public static void main(String[] args) {
        WindowsTheme.aplicarInicial();
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(); frame.setVisible(true); WindowsTheme.iniciarMonitor();
        });
    }
}
