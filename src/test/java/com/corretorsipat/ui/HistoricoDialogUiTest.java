package com.corretorsipat.ui;

import com.corretorsipat.history.HistoricoService;
import com.corretorsipat.model.RegistroHistorico;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica estados visuais e habilitação das ações do diálogo de histórico. */
class HistoricoDialogUiTest {
    @TempDir
    Path temporario;

    @Test
    void estadoVazioExibeMensagemEDesabilitaAcoesIncompativeis() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        JDialog dialog = criarDialog(criarService());
        try {
            JLabel mensagem = componentes(dialog, JLabel.class).stream()
                    .filter(label -> "Não existem arquivos no histórico.".equals(label.getText()))
                    .findFirst().orElseThrow();
            assertTrue(mensagem.isVisible());
            assertFalse(botao(dialog, "Selecionar").isEnabled());
            assertFalse(botao(dialog, "Detalhes").isEnabled());
            assertFalse(botao(dialog, "Excluir").isEnabled());
            assertFalse(botao(dialog, "Limpar histórico").isEnabled());
            assertTrue(botao(dialog, "Fechar").isEnabled());
        } finally {
            SwingUtilities.invokeAndWait(dialog::dispose);
        }
    }

    @Test
    void botoesTemIconeTooltipAcessibilidadeEReagemASelecao() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        HistoricoService service = criarService();
        Path arquivo = Files.createFile(temporario.resolve("referencia.sip"));
        service.adicionar(new RegistroHistorico(arquivo, LocalDateTime.now(), "ANALISADO", true));
        JDialog dialog = criarDialog(service);
        try {
            List<JButton> botoes = componentes(dialog, JButton.class).stream()
                    .filter(botao -> botao.getText() != null && !botao.getText().isBlank())
                    .toList();
            assertEquals(5, botoes.size());
            for (JButton botao : botoes) {
                assertNotNull(botao.getIcon(), botao.getText());
                assertNotNull(botao.getToolTipText(), botao.getText());
                assertNotNull(botao.getAccessibleContext().getAccessibleDescription(), botao.getText());
                assertEquals(7, botao.getIconTextGap(), botao.getText());
            }
            assertTrue(botao(dialog, "Limpar histórico").isEnabled());
            assertFalse(botao(dialog, "Excluir").isEnabled());
            JList<RegistroHistorico> lista = lista(dialog);
            SwingUtilities.invokeAndWait(() -> lista.setSelectedIndex(0));
            assertTrue(botao(dialog, "Selecionar").isEnabled());
            assertTrue(botao(dialog, "Detalhes").isEnabled());
            assertTrue(botao(dialog, "Excluir").isEnabled());
        } finally {
            SwingUtilities.invokeAndWait(dialog::dispose);
        }
    }

    private HistoricoService criarService() throws Exception {
        Constructor<HistoricoService> construtor = HistoricoService.class.getDeclaredConstructor(Path.class);
        construtor.setAccessible(true);
        return construtor.newInstance(temporario.resolve("Historico/historico.dat"));
    }

    private static JDialog criarDialog(HistoricoService service) throws Exception {
        Constructor<HistoricoDialog> construtor = HistoricoDialog.class.getDeclaredConstructor(Frame.class, HistoricoService.class);
        construtor.setAccessible(true);
        AtomicReference<JDialog> dialog = new AtomicReference<>();
        AtomicReference<Throwable> erro = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                dialog.set(construtor.newInstance(null, service));
            } catch (Throwable ex) {
                erro.set(ex);
            }
        });
        if (erro.get() != null) throw new AssertionError(erro.get());
        return dialog.get();
    }

    @SuppressWarnings("unchecked")
    private static JList<RegistroHistorico> lista(JDialog dialog) throws Exception {
        Field campo = HistoricoDialog.class.getDeclaredField("lista");
        campo.setAccessible(true);
        return (JList<RegistroHistorico>) campo.get(dialog);
    }

    private static JButton botao(Container raiz, String texto) {
        return componentes(raiz, JButton.class).stream()
                .filter(botao -> texto.equals(botao.getText()))
                .findFirst().orElseThrow();
    }

    private static <T extends Component> List<T> componentes(Container raiz, Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Component componente : raiz.getComponents()) {
            if (tipo.isInstance(componente)) encontrados.add(tipo.cast(componente));
            if (componente instanceof Container container) encontrados.addAll(componentes(container, tipo));
        }
        return encontrados;
    }
}
