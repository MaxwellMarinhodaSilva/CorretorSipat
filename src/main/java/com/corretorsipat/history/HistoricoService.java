package com.corretorsipat.history;

import com.corretorsipat.io.ArquivoTextoUtil;
import com.corretorsipat.io.DiretoriosAplicacao;
import com.corretorsipat.model.RegistroHistorico;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HistoricoService {
    private static final Logger LOGGER = Logger.getLogger(HistoricoService.class.getName());
    private final Path arquivo;

    public HistoricoService() {
        this(new DiretoriosAplicacao().arquivoHistorico());
    }

    HistoricoService(Path arquivo) {
        this.arquivo = arquivo;
    }

    public List<RegistroHistorico> carregar() {
        if (!Files.isRegularFile(arquivo)) return List.of();
        List<RegistroHistorico> itens = new ArrayList<>();
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                try {
                    String[] p = linha.split("\\|", -1);
                    Path path = Path.of(new String(Base64.getDecoder().decode(p[0]), StandardCharsets.UTF_8));
                    if (Files.exists(path))
                        itens.add(new RegistroHistorico(path, LocalDateTime.parse(p[1]), p[2], Boolean.parseBoolean(p[3])));
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Entrada inválida ignorada no histórico local.", ex);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Não foi possível ler o histórico local.", ex);
        }
        return List.copyOf(itens);
    }

    public void adicionar(RegistroHistorico novo) throws IOException {
        List<RegistroHistorico> itens = new ArrayList<>(carregar());
        itens.removeIf(item -> item.arquivo().equals(novo.arquivo()));
        itens.addFirst(novo);
        if (itens.size() > 20) itens = new ArrayList<>(itens.subList(0, 20));
        salvar(itens);
    }

    public void remover(RegistroHistorico registro) throws IOException {
        List<RegistroHistorico> itens = new ArrayList<>(carregar());
        itens.remove(registro);
        salvar(itens);
    }

    public void limpar() throws IOException {
        salvar(List.of());
    }

    private void salvar(List<RegistroHistorico> itens) throws IOException {
        StringBuilder texto = new StringBuilder();
        for (RegistroHistorico item : itens) {
            String path = Base64.getEncoder().encodeToString(item.arquivo().toString().getBytes(StandardCharsets.UTF_8));
            texto.append(path).append('|').append(item.dataHora()).append('|').append(item.operacao()).append('|').append(item.sucesso()).append('\n');
        }
        ArquivoTextoUtil.escreverAtomico(arquivo, texto.toString(), StandardCharsets.UTF_8);
    }
}
