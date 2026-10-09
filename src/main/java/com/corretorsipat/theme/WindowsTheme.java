package com.corretorsipat.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Aplica FlatLaf e acompanha a preferência clara ou escura do Windows.
 */
public final class WindowsTheme {
    private static final ScheduledExecutorService MONITOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "tema-windows");
        t.setDaemon(true);
        return t;
    });
    private static volatile boolean escuro = detectarEscuro();

    private WindowsTheme() {
    }

    public static void aplicarInicial() {
        FlatLaf.setup(escuro ? new FlatDarkLaf() : new FlatIntelliJLaf());
    }

    public static void iniciarMonitor() {
        MONITOR.scheduleAtFixedRate(() -> {
            boolean atual = detectarEscuro();
            if (atual == escuro) return;
            escuro = atual;
            SwingUtilities.invokeLater(() -> {
                FlatLaf.setup(atual ? new FlatDarkLaf() : new FlatIntelliJLaf());
                FlatLaf.updateUI();
                for (Window w : Window.getWindows()) SwingUtilities.updateComponentTreeUI(w);
            });
        }, 3, 3, TimeUnit.SECONDS);
    }

    public static void pararMonitor() {
        MONITOR.shutdownNow();
    }

    static boolean detectarEscuro() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) return false;
        try {
            Process p = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme").redirectErrorStream(true).start();
            if (!p.waitFor(3, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0 && new String(p.getInputStream().readAllBytes(), Charset.defaultCharset()).contains("0x0");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
