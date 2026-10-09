package com.corretorsipat.ui;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Confirma que o Windows recebe as variantes de ícone previstas para cada escala. */
class IconeAplicacaoTest {
    @Test
    void recursosDeIconePossuemTodosOsTamanhosWindows() {
        for (int tamanho : List.of(16, 20, 24, 32, 40, 48, 64, 128, 256)) {
            URL recurso = IconeAplicacao.class.getResource("/icones/aplicacao/corretor_sipat_" + tamanho + ".png");
            assertNotNull(recurso, tamanho + " px");
            ImageIcon icone = new ImageIcon(recurso);
            assertEquals(tamanho, icone.getIconWidth(), tamanho + " px de largura");
            assertEquals(tamanho, icone.getIconHeight(), tamanho + " px de altura");
        }
    }

    @Test
    void aplicaTodasAsVariantesAJanela() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() -> {
            JDialog janela = new JDialog();
            try {
                IconeAplicacao.aplicar(janela);
                assertEquals(List.of(16, 20, 24, 32, 40, 48, 64, 128, 256),
                        janela.getIconImages().stream().map(imagem -> imagem.getWidth(null)).toList());
                assertEquals(List.of(16, 20, 24, 32, 40, 48, 64, 128, 256),
                        janela.getIconImages().stream().map(imagem -> imagem.getHeight(null)).toList());
            } finally {
                janela.dispose();
            }
        });
    }
}
