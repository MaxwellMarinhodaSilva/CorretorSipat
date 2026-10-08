package com.corretorsipat.ui;

import com.corretorsipat.VersaoAplicacao;
import com.corretorsipat.config.Configuracao;
import com.corretorsipat.config.ConfiguracaoService;
import com.corretorsipat.history.HistoricoService;
import com.corretorsipat.model.AnaliseSipat;
import com.corretorsipat.model.RegistroHistorico;
import com.corretorsipat.model.ResultadoProcessamento;
import com.corretorsipat.service.AnaliseSipatService;
import com.corretorsipat.service.CorrecaoSipatService;
import com.corretorsipat.service.ProcessamentoException;
import com.corretorsipat.util.DesktopUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MainFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    private static final int TAMANHO_LOGO_CABECALHO = 56;
    private static final String INSTRUCAO_ARQUIVO = "Arraste e solte o arquivo SIPAT aqui ou clique em ‘Selecionar’.";
    private final ConfiguracaoService configuracaoService = new ConfiguracaoService();
    private final HistoricoService historicoService = new HistoricoService();
    private final Configuracao configuracao;
    private final JTextField campoArquivo = new JTextField();
    private final JButton analisar = new JButton("Analisar", IconeUtil.analisar());
    private final JButton corrigir = new JButton("Corrigir arquivo", IconeUtil.corrigir());
    private final JButton limpar = new JButton("Limpar", IconeUtil.limpar());
    private final JButton selecionar = new JButton("Selecionar", IconeUtil.sip());
    private final JButton historico = new JButton("Histórico", IconeUtil.historico());
    private final JProgressBar progresso = new JProgressBar(0, 100);
    private final JLabel status = new JLabel("Nenhum arquivo selecionado. Arraste um arquivo .SIP ou clique em Selecionar.");
    private final ResumoPanel resumo = new ResumoPanel();
    private final JButton abrirSip = new JButton("Abrir SIP corrigido", IconeUtil.sip());
    private final JButton abrirRelatorio = new JButton("Abrir relatório", IconeUtil.relatorio());
    private final JButton abrirPasta = new JButton("Abrir pasta", IconeUtil.pasta());
    private Path arquivoSelecionado;
    private ResultadoProcessamento ultimoResultado;

    public MainFrame() {
        super("Corretor SIPAT");
        configuracao = configuracaoService.carregar();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(1000, 650));
        setSize(Math.max(1000, configuracao.getLargura()), Math.max(650, configuracao.getAltura()));
        if (configuracao.getX() >= 0 && configuracao.getY() >= 0) setLocation(configuracao.getX(), configuracao.getY());
        else setLocationRelativeTo(null);
        var recurso = MainFrame.class.getResource("/icones/corretor_sipat_32.png");
        if (recurso != null) setIconImage(new ImageIcon(recurso).getImage());
        montar();
        configurarEventos();
        configurarAcessibilidade();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fechar();
            }
        });
    }

    private static Throwable causa(Exception ex) {
        return ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
    }

    private void montar() {
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        JPanel topo = new JPanel();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.add(criarCabecalho());
        topo.add(Box.createVerticalStrut(15));
        topo.add(criarSelecao());
        topo.add(Box.createVerticalStrut(12));
        topo.add(resumo.indicadores());
        raiz.add(topo, BorderLayout.NORTH);

        raiz.add(resumo, BorderLayout.CENTER);

        JPanel sul = new JPanel();
        sul.setLayout(new BoxLayout(sul, BoxLayout.Y_AXIS));
        sul.add(criarAcoes());
        sul.add(Box.createVerticalStrut(8));
        JPanel progressoPainel = new JPanel(new BorderLayout(0, 4));
        progresso.setStringPainted(true);
        progresso.setString("0%");
        progressoPainel.add(status, BorderLayout.NORTH);
        progressoPainel.add(progresso, BorderLayout.CENTER);
        sul.add(progressoPainel);
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 7));
        JLabel versao = new JLabel(VersaoAplicacao.RODAPE);
        versao.setForeground(Color.GRAY);
        versao.setFont(versao.getFont().deriveFont(10f));
        rodape.add(versao);
        sul.add(rodape);
        raiz.add(sul, BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout(10, 0));
        cabecalho.setAlignmentX(LEFT_ALIGNMENT);
        JLabel logo = criarLogoCabecalho();
        if (logo != null) cabecalho.add(logo, BorderLayout.WEST);
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Corretor SIPAT");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 25f));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitulo = new JLabel("Analise protocolos repetidos e gere uma cópia estruturalmente validada do arquivo SIPAT.");
        subtitulo.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(subtitulo);
        cabecalho.add(textos, BorderLayout.CENTER);
        return cabecalho;
    }

    private JLabel criarLogoCabecalho() {
        var recurso = MainFrame.class.getResource("/imagens/Logo_Corretor_SIPAT.png");
        if (recurso == null) {
            LOGGER.warning("Não foi possível localizar a logo do cabeçalho no classpath.");
            return null;
        }
        ImageIcon original = new ImageIcon(recurso);
        int larguraOriginal = original.getIconWidth();
        int alturaOriginal = original.getIconHeight();
        if (larguraOriginal <= 0 || alturaOriginal <= 0) {
            LOGGER.warning("Não foi possível carregar a logo do cabeçalho.");
            return null;
        }
        double escala = Math.min((double) TAMANHO_LOGO_CABECALHO / larguraOriginal,
                (double) TAMANHO_LOGO_CABECALHO / alturaOriginal);
        int largura = Math.max(1, (int) Math.round(larguraOriginal * escala));
        int altura = Math.max(1, (int) Math.round(alturaOriginal * escala));
        JLabel logo = new JLabel(new ImageIcon(original.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH)));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setVerticalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(TAMANHO_LOGO_CABECALHO, TAMANHO_LOGO_CABECALHO));
        logo.setToolTipText("Logo do Corretor SIPAT");
        return logo;
    }

    private JPanel criarSelecao() {
        JPanel bloco = new JPanel(new BorderLayout(8, 7));
        bloco.setAlignmentX(LEFT_ALIGNMENT);
        JLabel label = new JLabel("Arquivo SIPAT");
        bloco.add(label, BorderLayout.NORTH);
        campoArquivo.setEditable(false);
        campoArquivo.setPreferredSize(new Dimension(100, 36));
        exibirInstrucaoArquivo();
        campoArquivo.setToolTipText("Arquivo SIPAT selecionado ou área para arrastar e soltar.");
        Border normal = campoArquivo.getBorder();
        Border destaque = BorderFactory.createLineBorder(new Color(55, 125, 220), 2);
        ArquivoTransferHandler transfer = new ArquivoTransferHandler(campoArquivo, normal, destaque, f -> definirArquivo(f.toPath()));
        campoArquivo.setTransferHandler(transfer);
        JPanel botoes = new JPanel(new GridLayout(1, 2, 7, 0));
        botoes.add(selecionar);
        botoes.add(historico);
        bloco.add(campoArquivo, BorderLayout.CENTER);
        bloco.add(botoes, BorderLayout.EAST);
        return bloco;
    }

    private JPanel criarAcoes() {
        JPanel linha = new JPanel(new BorderLayout());
        JPanel principais = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        analisar.setPreferredSize(new Dimension(130, 38));
        corrigir.setPreferredSize(new Dimension(175, 38));
        limpar.setPreferredSize(new Dimension(105, 38));
        principais.add(analisar);
        principais.add(corrigir);
        principais.add(limpar);
        JPanel saidas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        for (JButton b : new JButton[]{abrirSip, abrirRelatorio, abrirPasta}) {
            b.setEnabled(false);
            saidas.add(b);
        }
        linha.add(principais, BorderLayout.WEST);
        linha.add(saidas, BorderLayout.EAST);
        return linha;
    }

    private void configurarEventos() {
        selecionar.addActionListener(e -> selecionarArquivo());
        historico.addActionListener(e -> {
            Path p = HistoricoDialog.selecionar(this, historicoService);
            if (p != null) definirArquivo(p);
        });
        limpar.addActionListener(e -> limparSessao());
        analisar.addActionListener(e -> executarAnalise());
        corrigir.addActionListener(e -> executarCorrecao());
        abrirSip.addActionListener(e -> DesktopUtil.abrir(this, ultimoResultado.arquivos().sipCorrigido()));
        abrirRelatorio.addActionListener(e -> DesktopUtil.abrir(this, ultimoResultado.arquivos().relatorioTxt()));
        abrirPasta.addActionListener(e -> DesktopUtil.abrir(this, ultimoResultado.arquivos().pastaBase()));
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control L"), "limparSessao");
        getRootPane().getActionMap().put("limparSessao", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (limpar.isEnabled()) limparSessao();
            }
        });
    }

    private void selecionarArquivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar arquivo SIPAT");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivo SIPAT (*.SIP)", "SIP", "sip"));
        if (!configuracao.getUltimaPasta().isBlank()) {
            File pasta = new File(configuracao.getUltimaPasta());
            if (pasta.isDirectory()) chooser.setCurrentDirectory(pasta);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            definirArquivo(chooser.getSelectedFile().toPath());
    }

    private void definirArquivo(Path arquivo) {
        if (!Files.isRegularFile(arquivo) || !Files.isReadable(arquivo) || !arquivo.getFileName().toString().toLowerCase().endsWith(".sip")) {
            MensagemUtil.erro(this, "Selecione um arquivo .SIP válido e legível.");
            return;
        }
        arquivoSelecionado = arquivo.toAbsolutePath().normalize();
        campoArquivo.setText(arquivoSelecionado.toString());
        campoArquivo.setForeground(UIManager.getColor("TextField.foreground"));
        campoArquivo.setToolTipText(arquivoSelecionado.toString());
        configuracao.setUltimaPasta(arquivoSelecionado.getParent().toString());
        resumo.limpar();
        ultimoResultado = null;
        habilitarSaidas(false);
        status.setText("Arquivo pronto para análise ou correção.");
        salvarConfiguracaoSilenciosa();
    }

    private boolean possuiArquivo() {
        if (arquivoSelecionado == null) {
            MensagemUtil.aviso(this, "Selecione um arquivo .SIP primeiro.");
            return false;
        }
        return true;
    }

    private void limparSessao() {
        arquivoSelecionado = null;
        ultimoResultado = null;
        exibirInstrucaoArquivo();
        campoArquivo.setToolTipText("Arquivo SIPAT selecionado ou área para arrastar e soltar.");
        resumo.limpar();
        habilitarSaidas(false);
        progresso.setValue(0);
        progresso.setString("0%");
        status.setText("Nenhum arquivo selecionado. Arraste um arquivo .SIP ou clique em Selecionar.");
    }

    private void executarAnalise() {
        if (!possuiArquivo()) return;
        setOcupado(true, "Analisando estrutura e protocolos...");
        new SwingWorker<AnaliseSipat, Void>() {
            @Override
            protected AnaliseSipat doInBackground() throws Exception {
                return new AnaliseSipatService().analisar(arquivoSelecionado, (p, etapa) -> atualizarProgresso(p, etapa));
            }

            @Override
            protected void done() {
                try {
                    AnaliseSipat a = get();
                    resumo.exibir(a);
                    status.setText("Análise concluída: " + a.ocorrenciasExcedentes() + " ocorrência(s) excedente(s), " + a.inconsistencias().size() + " inconsistência(s).");
                    progresso.setValue(100);
                    progresso.setString("100%");
                    registrar("ANÁLISE", true);
                } catch (Exception ex) {
                    Throwable causa = causa(ex);
                    status.setText("Não foi possível analisar o arquivo.");
                    MensagemUtil.erro(MainFrame.this, causa.getMessage() == null ? "Arquivo SIPAT inválido." : causa.getMessage());
                    registrar("ANÁLISE", false);
                } finally {
                    setOcupado(false, null);
                }
            }
        }.execute();
    }

    private void executarCorrecao() {
        if (!possuiArquivo()) return;
        setOcupado(true, "Preparando a correção...");
        new SwingWorker<ResultadoProcessamento, Void>() {
            @Override
            protected ResultadoProcessamento doInBackground() throws Exception {
                return new CorrecaoSipatService().corrigir(arquivoSelecionado, (p, etapa) -> atualizarProgresso(p, etapa));
            }

            @Override
            protected void done() {
                try {
                    ultimoResultado = get();
                    resumo.exibir(ultimoResultado.analise());
                    habilitarSaidas(true);
                    status.setText("Cópia corrigida, relatórios e log gerados com validação concluída.");
                    progresso.setValue(100);
                    progresso.setString("100%");
                    registrar("CORREÇÃO", true);
                    new ProcessamentoConcluidoDialog(MainFrame.this, ultimoResultado).setVisible(true);
                } catch (Exception ex) {
                    Throwable causa = causa(ex);
                    status.setText("A correção não foi concluída.");
                    registrar("CORREÇÃO", false);
                    if (causa instanceof ProcessamentoException pe)
                        new ErroProcessamentoDialog(MainFrame.this, pe.getMessage(), pe.arquivoLog()).setVisible(true);
                    else new ErroProcessamentoDialog(MainFrame.this, causa.getMessage(), null).setVisible(true);
                } finally {
                    setOcupado(false, null);
                }
            }
        }.execute();
    }

    private void atualizarProgresso(int valor, String etapa) {
        SwingUtilities.invokeLater(() -> {
            progresso.setValue(valor);
            progresso.setString(valor + "%");
            status.setText(etapa);
        });
    }

    private void setOcupado(boolean ocupado, String texto) {
        analisar.setEnabled(!ocupado);
        corrigir.setEnabled(!ocupado);
        limpar.setEnabled(!ocupado);
        selecionar.setEnabled(!ocupado);
        historico.setEnabled(!ocupado);
        if (ocupado) {
            progresso.setValue(0);
            progresso.setString("0%");
            if (texto != null) status.setText(texto);
        }
    }

    private void habilitarSaidas(boolean habilitar) {
        abrirSip.setEnabled(habilitar);
        abrirRelatorio.setEnabled(habilitar);
        abrirPasta.setEnabled(habilitar);
    }

    private void registrar(String operacao, boolean sucesso) {
        try {
            historicoService.adicionar(new RegistroHistorico(arquivoSelecionado, LocalDateTime.now(), operacao, sucesso));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Não foi possível atualizar o histórico local.", ex);
        }
    }

    private void fechar() {
        configuracao.setX(getX());
        configuracao.setY(getY());
        configuracao.setLargura(getWidth());
        configuracao.setAltura(getHeight());
        salvarConfiguracaoSilenciosa();
        com.corretorsipat.theme.WindowsTheme.pararMonitor();
        dispose();
        System.exit(0);
    }

    private void salvarConfiguracaoSilenciosa() {
        try {
            configuracaoService.salvar(configuracao);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Não foi possível salvar as preferências locais.", ex);
        }
    }

    private void configurarAcessibilidade() {
        configurarBotao(analisar, "Analisar o arquivo SIPAT selecionado");
        configurarBotao(corrigir, "Gerar uma cópia corrigida do arquivo SIPAT selecionado");
        configurarBotao(limpar, "Limpar somente os dados da sessão atual sem apagar o histórico ou arquivos");
        configurarBotao(selecionar, "Selecionar um arquivo SIPAT");
        configurarBotao(historico, "Abrir o histórico local de arquivos");
        configurarBotao(abrirSip, "Abrir o SIP corrigido gerado no último processamento");
        configurarBotao(abrirRelatorio, "Abrir o relatório TXT gerado no último processamento");
        configurarBotao(abrirPasta, "Abrir a pasta dos arquivos auxiliares gerados");
        campoArquivo.getAccessibleContext().setAccessibleName("Arquivo SIPAT selecionado");
        campoArquivo.getAccessibleContext().setAccessibleDescription(
                "Arraste e solte o arquivo SIPAT nesta área ou use o botão Selecionar.");
    }

    private void exibirInstrucaoArquivo() {
        campoArquivo.setText(INSTRUCAO_ARQUIVO);
        Color cor = UIManager.getColor("Label.disabledForeground");
        if (cor != null) campoArquivo.setForeground(cor);
    }

    private static void configurarBotao(JButton botao, String dica) {
        botao.setIconTextGap(7);
        botao.setToolTipText(dica);
        botao.getAccessibleContext().setAccessibleName(botao.getText());
        botao.getAccessibleContext().setAccessibleDescription(dica);
    }
}
