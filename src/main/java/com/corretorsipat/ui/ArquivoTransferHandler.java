package com.corretorsipat.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Recebe arrastar e soltar de um único arquivo SIP legível na área de seleção.
 */
public final class ArquivoTransferHandler extends TransferHandler {
    private final JComponent alvo;
    private final Border normal;
    private final Border destaque;
    private final Consumer<File> consumidor;

    public ArquivoTransferHandler(JComponent alvo, Border normal, Border destaque, Consumer<File> consumidor) {
        this.alvo = alvo;
        this.normal = normal;
        this.destaque = destaque;
        this.consumidor = consumidor;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        boolean aceita = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        if (support.isDrop()) alvo.setBorder(aceita ? destaque : normal);
        return aceita;
    }

    @Override
    public boolean importData(TransferSupport support) {
        alvo.setBorder(normal);
        if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false;
        try {
            List<?> itens = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (itens.isEmpty() || !(itens.getFirst() instanceof File arquivo)) return false;
            if (!arquivo.isFile() || !arquivo.canRead() || !arquivo.getName().toLowerCase().endsWith(".sip")) {
                MensagemUtil.erro(alvo, "Arraste um arquivo .SIP válido e legível.");
                return false;
            }
            consumidor.accept(arquivo);
            return true;
        } catch (Exception ex) {
            MensagemUtil.erro(alvo, "Não foi possível receber o arquivo arrastado.");
            return false;
        }
    }
}
