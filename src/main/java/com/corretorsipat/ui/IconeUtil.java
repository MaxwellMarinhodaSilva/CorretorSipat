package com.corretorsipat.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** Cria ícones vetoriais coerentes para as ações da interface Swing. */
public final class IconeUtil {
    private static final int TAMANHO = 18;

    private IconeUtil() {
    }

    public static Icon pasta() {
        return icone((g, x, y) -> {
            Path2D p = new Path2D.Double();
            p.moveTo(x + 2, y + 5);
            p.lineTo(x + 7, y + 5);
            p.lineTo(x + 9, y + 3);
            p.lineTo(x + 16, y + 3);
            p.lineTo(x + 16, y + 15);
            p.lineTo(x + 2, y + 15);
            p.closePath();
            g.draw(p);
        });
    }

    public static Icon analisar() {
        return icone((g, x, y) -> {
            g.draw(new Ellipse2D.Double(x + 2, y + 2, 10, 10));
            g.drawLine(x + 11, y + 11, x + 16, y + 16);
        });
    }

    public static Icon corrigir() {
        return icone((g, x, y) -> {
            g.draw(new RoundRectangle2D.Double(x + 2, y + 2, 14, 14, 3, 3));
            g.drawLine(x + 5, y + 10, x + 8, y + 13);
            g.drawLine(x + 8, y + 13, x + 14, y + 6);
        });
    }

    public static Icon historico() {
        return icone((g, x, y) -> {
            g.draw(new Ellipse2D.Double(x + 3, y + 3, 12, 12));
            g.drawLine(x + 9, y + 6, x + 9, y + 10);
            g.drawLine(x + 9, y + 10, x + 12, y + 11);
        });
    }

    public static Icon abrir() {
        return icone((g, x, y) -> {
            Path2D p = new Path2D.Double();
            p.moveTo(x + 2, y + 6);
            p.lineTo(x + 7, y + 6);
            p.lineTo(x + 9, y + 4);
            p.lineTo(x + 15, y + 4);
            p.lineTo(x + 15, y + 14);
            p.lineTo(x + 2, y + 14);
            p.closePath();
            g.draw(p);
            g.drawLine(x + 9, y + 9, x + 16, y + 9);
            g.drawLine(x + 13, y + 6, x + 16, y + 9);
            g.drawLine(x + 13, y + 12, x + 16, y + 9);
        });
    }

    public static Icon detalhes() {
        return icone((g, x, y) -> {
            desenharDocumento(g, x, y);
            g.draw(new Ellipse2D.Double(x + 5, y + 7, 6, 6));
            g.drawLine(x + 10, y + 12, x + 14, y + 16);
        });
    }

    public static Icon excluir() {
        return icone((g, x, y) -> {
            g.draw(new RoundRectangle2D.Double(x + 5, y + 6, 8, 10, 2, 2));
            g.drawLine(x + 4, y + 6, x + 14, y + 6);
            g.drawLine(x + 7, y + 4, x + 11, y + 4);
            g.drawLine(x + 8, y + 9, x + 8, y + 13);
            g.drawLine(x + 10, y + 9, x + 10, y + 13);
        });
    }

    public static Icon limparHistorico() {
        return icone((g, x, y) -> {
            g.draw(new RoundRectangle2D.Double(x + 6, y + 6, 8, 10, 2, 2));
            g.drawLine(x + 5, y + 6, x + 15, y + 6);
            g.drawLine(x + 8, y + 4, x + 12, y + 4);
            g.drawLine(x + 2, y + 9, x + 5, y + 9);
            g.drawLine(x + 2, y + 12, x + 5, y + 12);
            g.drawLine(x + 2, y + 15, x + 5, y + 15);
        });
    }

    public static Icon limpar() {
        return icone((g, x, y) -> {
            Path2D borracha = new Path2D.Double();
            borracha.moveTo(x + 3, y + 12);
            borracha.lineTo(x + 9, y + 6);
            borracha.quadTo(x + 10, y + 5, x + 11, y + 6);
            borracha.lineTo(x + 15, y + 10);
            borracha.quadTo(x + 16, y + 11, x + 15, y + 12);
            borracha.lineTo(x + 10, y + 17);
            borracha.lineTo(x + 3, y + 17);
            borracha.closePath();
            g.draw(borracha);
            g.drawLine(x + 7, y + 16, x + 14, y + 9);
            g.drawLine(x + 3, y + 17, x + 16, y + 17);
        });
    }

    public static Icon erro() {
        return icone((g, x, y) -> {
            g.draw(new Ellipse2D.Double(x + 2, y + 2, 14, 14));
            g.drawLine(x + 6, y + 6, x + 12, y + 12);
            g.drawLine(x + 12, y + 6, x + 6, y + 12);
        });
    }

    public static Icon copiar() {
        return icone((g, x, y) -> {
            g.draw(new RoundRectangle2D.Double(x + 5, y + 2, 10, 12, 2, 2));
            g.draw(new RoundRectangle2D.Double(x + 2, y + 5, 10, 12, 2, 2));
        });
    }

    public static Icon fechar() {
        return icone((g, x, y) -> {
            g.drawLine(x + 4, y + 4, x + 14, y + 14);
            g.drawLine(x + 14, y + 4, x + 4, y + 14);
        });
    }

    public static Icon sip() {
        return icone((g, x, y) -> {
            desenharDocumento(g, x, y);
            g.drawLine(x + 6, y + 10, x + 12, y + 10);
            g.drawLine(x + 6, y + 13, x + 11, y + 13);
        });
    }

    public static Icon relatorio() {
        return icone((g, x, y) -> {
            desenharDocumento(g, x, y);
            g.drawLine(x + 6, y + 8, x + 12, y + 8);
            g.drawLine(x + 6, y + 11, x + 12, y + 11);
            g.drawLine(x + 6, y + 14, x + 10, y + 14);
        });
    }

    public static Icon planilha() {
        return icone((g, x, y) -> {
            g.draw(new Rectangle2D.Double(x + 2, y + 3, 14, 13));
            g.drawLine(x + 2, y + 7, x + 16, y + 7);
            g.drawLine(x + 2, y + 11, x + 16, y + 11);
            g.drawLine(x + 7, y + 3, x + 7, y + 16);
            g.drawLine(x + 12, y + 3, x + 12, y + 16);
        });
    }

    public static Icon log() {
        return icone((g, x, y) -> {
            desenharDocumento(g, x, y);
            g.draw(new Ellipse2D.Double(x + 5, y + 8, 1, 1));
            g.drawLine(x + 8, y + 9, x + 12, y + 9);
            g.draw(new Ellipse2D.Double(x + 5, y + 12, 1, 1));
            g.drawLine(x + 8, y + 13, x + 12, y + 13);
        });
    }

    private static void desenharDocumento(Graphics2D g, int x, int y) {
        Path2D documento = new Path2D.Double();
        documento.moveTo(x + 3, y + 2);
        documento.lineTo(x + 11, y + 2);
        documento.lineTo(x + 15, y + 6);
        documento.lineTo(x + 15, y + 16);
        documento.lineTo(x + 3, y + 16);
        documento.closePath();
        g.draw(documento);
        g.drawLine(x + 11, y + 2, x + 11, y + 6);
        g.drawLine(x + 11, y + 6, x + 15, y + 6);
    }

    private static Icon icone(Desenho desenho) {
        return new Icon() {
            public int getIconWidth() {
                return TAMANHO;
            }

            public int getIconHeight() {
                return TAMANHO;
            }

            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Color disabled = UIManager.getColor("Label.disabledForeground");
                g.setColor(c.isEnabled() ? c.getForeground() : disabled == null ? Color.GRAY : disabled);
                desenho.desenhar(g, x, y);
                g.dispose();
            }
        };
    }

    @FunctionalInterface
    private interface Desenho {
        void desenhar(Graphics2D g, int x, int y);
    }
}
