package com.corretorsipat.io;

import com.corretorsipat.model.ArquivosGerados;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PlanoArquivosSaida {
    private final DiretoriosAplicacao diretorios;

    public PlanoArquivosSaida() {
        this(new DiretoriosAplicacao());
    }

    PlanoArquivosSaida(DiretoriosAplicacao diretorios) {
        this.diretorios = diretorios;
    }

    public ArquivosGerados criar(Path origem) throws IOException {
        Path origemAbsoluta = origem.toAbsolutePath().normalize();
        Path pasta = origemAbsoluta.getParent();
        if (pasta == null || !Files.isDirectory(pasta) || !Files.isWritable(pasta)) {
            throw new IOException("A pasta do arquivo não permite gravar a saída.");
        }
        diretorios.prepararPastasDeSaida();
        String nome = origemAbsoluta.getFileName().toString();
        int ponto = nome.lastIndexOf('.');
        String base = ponto > 0 ? nome.substring(0, ponto) : nome;
        for (int indice = 1; indice < 10_000; indice++) {
            String sufixo = indice == 1 ? "" : "_" + indice;
            Path sip = pasta.resolve(base + "_SEM_DUPLICADOS" + sufixo + ".SIP");
            Path txt = diretorios.pastaRelatoriosTxt().resolve(base + "_RELATORIO" + sufixo + ".txt");
            Path csv = diretorios.pastaRelatoriosCsv().resolve(base + "_LINHAS_REMOVIDAS" + sufixo + ".csv");
            Path log = diretorios.pastaLogs().resolve(base + "_PROCESSAMENTO" + sufixo + ".log");
            if (!Files.exists(sip) && !Files.exists(txt) && !Files.exists(csv) && !Files.exists(log)) {
                return new ArquivosGerados(sip, txt, csv, log);
            }
        }
        throw new IOException("Não foi possível gerar nomes livres para os arquivos de saída.");
    }
}
