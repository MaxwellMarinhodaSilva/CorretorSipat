package com.corretorsipat.ui;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Window;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Fornece as variantes do ícone da aplicação adequadas às escalas do Windows. */
final class IconeAplicacao {
    private static final List<Integer> TAMANHOS = List.of(16, 20, 24, 32, 40, 48, 64, 128, 256);
    private static final List<Image> IMAGENS = carregar();

    private IconeAplicacao() {
    }

    static void aplicar(Window janela) {
        if (!IMAGENS.isEmpty()) janela.setIconImages(IMAGENS);
    }

    private static List<Image> carregar() {
        List<Image> imagens = new ArrayList<>();
        for (int tamanho : TAMANHOS) {
            URL recurso = IconeAplicacao.class.getResource("/icones/aplicacao/corretor_sipat_" + tamanho + ".png");
            if (recurso != null) imagens.add(new ImageIcon(recurso).getImage());
        }
        return List.copyOf(imagens);
    }
}
