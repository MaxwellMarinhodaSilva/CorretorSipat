package com.corretorsipat.history;

import com.corretorsipat.model.RegistroHistorico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica persistência, exclusão individual e limpeza do histórico temporário. */
class HistoricoServiceTest {
    @TempDir
    Path temporario;

    @Test
    void removeSomenteORegistroSelecionado() throws Exception {
        Path primeiro = Files.createFile(temporario.resolve("primeiro.sip"));
        Path segundo = Files.createFile(temporario.resolve("segundo.sip"));
        HistoricoService service = new HistoricoService(temporario.resolve("Historico/historico.dat"));
        RegistroHistorico registroPrimeiro = new RegistroHistorico(primeiro, LocalDateTime.of(2026, 10, 7, 10, 0), "CORRIGIDO", true);
        RegistroHistorico registroSegundo = new RegistroHistorico(segundo, LocalDateTime.of(2026, 10, 7, 11, 0), "ANALISADO", true);

        service.adicionar(registroPrimeiro);
        service.adicionar(registroSegundo);
        service.remover(registroPrimeiro);

        assertEquals(1, service.carregar().size());
        assertEquals(registroSegundo, service.carregar().getFirst());
    }

    @Test
    void limparRemoveTodosOsRegistros() throws Exception {
        Path arquivo = Files.createFile(temporario.resolve("arquivo.sip"));
        HistoricoService service = new HistoricoService(temporario.resolve("Historico/historico.dat"));
        service.adicionar(new RegistroHistorico(arquivo, LocalDateTime.now(), "ANALISADO", true));

        service.limpar();

        assertTrue(service.carregar().isEmpty());
    }
}
