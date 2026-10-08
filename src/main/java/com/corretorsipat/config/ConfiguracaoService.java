package com.corretorsipat.config;

import com.corretorsipat.io.ArquivoTextoUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Carrega e persiste preferências locais sem participar das regras SIPAT. */
public final class ConfiguracaoService {
    private final Path arquivo;

    public ConfiguracaoService() {
        this(Path.of(System.getProperty("user.dir"), "config.properties"));
    }

    ConfiguracaoService(Path arquivo) {
        this.arquivo = arquivo;
    }

    private static int inteiro(Properties p, String chave, int padrao, int minimo, int maximo) {
        try {
            return Math.max(minimo, Math.min(maximo, Integer.parseInt(p.getProperty(chave))));
        } catch (Exception ex) {
            return padrao;
        }
    }

    public Configuracao carregar() {
        Configuracao c = new Configuracao();
        if (!Files.isRegularFile(arquivo)) return c;
        Properties p = new Properties();
        try (var in = Files.newInputStream(arquivo)) {
            p.load(in);
            c.setUltimaPasta(p.getProperty("ultimaPasta", ""));
            c.setX(inteiro(p, "x", -1, -100_000, 100_000));
            c.setY(inteiro(p, "y", -1, -100_000, 100_000));
            c.setLargura(inteiro(p, "largura", 1050, 850, 10_000));
            c.setAltura(inteiro(p, "altura", 760, 650, 10_000));
        } catch (Exception ignored) {
            return new Configuracao();
        }
        return c;
    }

    public void salvar(Configuracao c) throws IOException {
        Properties p = new Properties();
        p.setProperty("ultimaPasta", c.getUltimaPasta());
        p.setProperty("x", Integer.toString(c.getX()));
        p.setProperty("y", Integer.toString(c.getY()));
        p.setProperty("largura", Integer.toString(c.getLargura()));
        p.setProperty("altura", Integer.toString(c.getAltura()));
        StringWriter out = new StringWriter();
        p.store(out, "Configurações do Corretor SIPAT");
        ArquivoTextoUtil.escreverAtomico(arquivo, out.toString(), StandardCharsets.ISO_8859_1);
    }
}
