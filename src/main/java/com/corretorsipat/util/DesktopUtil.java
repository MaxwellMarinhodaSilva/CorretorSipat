package com.corretorsipat.util;

import com.corretorsipat.ui.MensagemUtil;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abre com segurança arquivos ou pastas usando a integração nativa do sistema operacional.
 */
public final class DesktopUtil {
    private DesktopUtil() {
    }

    public static void abrir(Component parent, Path caminho) {
        try {
            if (caminho == null || !Files.exists(caminho)) {
                MensagemUtil.erro(parent, "O arquivo ou pasta não foi encontrado.");
                return;
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                MensagemUtil.erro(parent, "A abertura de arquivos não é suportada neste sistema.");
                return;
            }
            Desktop.getDesktop().open(caminho.toFile());
        } catch (Exception ex) {
            MensagemUtil.erro(parent, "Não foi possível abrir o item selecionado.");
        }
    }
}
