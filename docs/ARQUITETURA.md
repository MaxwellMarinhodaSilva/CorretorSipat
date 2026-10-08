# Arquitetura do Corretor SIPAT

## 1. Decisões de referência

Este documento foi definido antes das regras de negócio e da interface. A leitura dos projetos `IndicadorReal` e `ConsultaSeloDigital` levou às seguintes decisões:

- manter ponto de entrada pequeno e uma janela principal Swing;
- usar FlatLaf com detecção e monitoramento do tema claro/escuro do Windows;
- executar análise e correção fora da EDT com `SwingWorker`;
- separar seleção/drag-and-drop, mensagens, abertura pelo Desktop, preferências e histórico;
- modelar o resultado completo do processamento uma única vez e entregá-lo à janela de conclusão e aos geradores TXT/CSV;
- persistir por arquivo temporário no mesmo diretório e finalizar por movimento atômico quando suportado;
- oferecer diálogo modal redimensionável, rolável e centralizado para o resumo final;
- empacotar JAR com dependências e imagem de aplicação Windows por Maven/jpackage.

O domínio SIPAT é implementado em classes próprias. Nenhum código-fonte ou arquivo dos projetos de referência será modificado.

## 2. Pacotes e responsabilidades

### `com.corretorsipat`

- `CorretorSipat`: ponto de entrada; aplica tema, abre a janela principal e inicia o monitor do Windows.
- `VersaoAplicacao`: versão e nome visível centralizados.

### `config`

- `Configuracao`: preferências mutáveis exclusivamente para serialização local da interface.
- `ConfiguracaoService`: carrega e salva `config.properties` de forma atômica, com valores padrão seguros.

### `io`

- `ArquivoSipatReader`: lê bytes, detecta BOM, codificação e quebras de linha sem alterar conteúdo.
- `ArquivoSipatWriter`: grava Windows-1252 sem BOM, CRLF inclusive após a última linha, em temporário e com movimento atômico.
- `ArquivoTextoUtil`: escrita atômica UTF-8 para relatórios, histórico, configuração e logs.
- `DiretoriosAplicacao`: centraliza a pasta-base e os diretórios de histórico, logs e relatórios.
- `PlanoArquivosSaida`: mantém o SIP ao lado da entrada e calcula nomes sem colisão para SIP, TXT, CSV e log; nunca substitui silenciosamente.

### `model`

Modelos imutáveis, preferencialmente `record`:

- `ArquivoSip`: caminho, codificação, quebra de linha, BOM e linhas físicas.
- `RegistroSip`: número físico original, tipo, conteúdo integral e campos extraídos quando aplicáveis.
- `LinhaRemovida`: protocolo, linha removida, linha preservada, valor, controle do devedor, classificação e motivo.
- `GrupoDuplicidade`: protocolo, linha preservada e ocorrências removidas.
- `Inconsistencia`: categoria e descrição de uma divergência estrutural.
- `AnaliseSipat`: todas as métricas, grupos, registros preservados e valores estruturais antes/depois.
- `CorrecaoCampo`: campo, valor anterior e posterior.
- `ItemValidacao`: verificação pós-gravação, sucesso e detalhe.
- `ResultadoValidacao`: conjunto imutável de itens e estado consolidado.
- `ArquivosGerados`: caminhos dos quatro artefatos.
- `ResultadoProcessamento`: objeto único consumido pela interface e pelos relatórios.
- `RegistroHistorico`: entrada persistida do histórico.

Listas recebidas pelos records são copiadas defensivamente em seus construtores.

### `parser`

- `LayoutSipat`: única fonte de posições e tamanhos. Usa índices Java `[início, fim)` correspondentes às posições humanas documentadas.
- `SipatParser`: valida/extrai tipo, valor, protocolo e sequencial; substitui campos fixos garantindo 800 caracteres.

Não haverá substring, índice posicional ou formatação numérica do layout na interface.

### `service`

- `SipatValidationException`: erro de entrada com mensagem adequada ao usuário.
- `AnaliseSipatService`: valida estrutura, mantém a primeira ocorrência por `LinkedHashMap`, classifica duplicidades, calcula somas e inconsistências.
- `CorrecaoSipatService`: orquestra análise, reconstrução, campos de controle, renumeração, gravação, validação independente e relatórios.
- `ValidacaoPosGravacaoService`: reabre os bytes produzidos e verifica comprimento, unicidade, somas, quantidades, sequenciais, Windows-1252, BOM e CRLF final.
- `ProgressoListener`: contrato de atualização de etapa/percentual sem dependência de Swing.

### `report`

- `RelatorioService`: transforma exclusivamente `ResultadoProcessamento` em TXT e CSV e persiste os dois.
- `ResumoTextoService`: gera o texto de “Copiar resumo” a partir do mesmo resultado.

### `history`

- `HistoricoService`: mantém as entradas mais recentes em UTF-8, elimina duplicatas por caminho e tolera registros antigos inválidos.

### `log`

- `ProcessamentoLog`: log por processamento, `AutoCloseable`, com caminho conhecido desde o início e stack trace somente no arquivo.

### `theme`

- `WindowsTheme`: aplica FlatLaf claro/escuro e atualiza janelas quando a preferência do Windows mudar.

### `ui`

- `MainFrame`: composição visual e coordenação dos workers; não contém regra posicional.
- `ProcessamentoConcluidoDialog`: resumo modal, tabela copiável/ordenável, correções e ações de abertura.
- `ErroProcessamentoDialog`: mensagem simples, caminho e abertura do log.
- `HistoricoDialog`: seleção e limpeza do histórico.
- `ArquivoTransferHandler`: drag-and-drop de um `.SIP` legível.
- `DuplicidadesTableModel` e `LinhasRemovidasTableModel`: adaptadores de apresentação sem cálculos de domínio.
- `ResumoPanel`: cartões e tabela da análise.
- `IconeUtil`, `MensagemUtil`: elementos visuais coesos.

### `util`

- `DesktopUtil`: abertura segura de arquivo/pasta.
- `HashUtil`: SHA-256 por fluxo.
- `FormatUtil`: zeros à esquerda e formatação visual sem conhecimento do layout.

## 3. Contratos principais

```java
ArquivoSip ArquivoSipatReader.ler(Path origem) throws IOException;
AnaliseSipat AnaliseSipatService.analisar(Path origem, ProgressoListener progresso)
        throws IOException, SipatValidationException;
ResultadoProcessamento CorrecaoSipatService.corrigir(Path origem, ProgressoListener progresso)
        throws IOException, SipatValidationException;
ResultadoValidacao ValidacaoPosGravacaoService.validar(Path arquivoCorrigido);
String RelatorioService.gerarTxt(ResultadoProcessamento resultado);
String RelatorioService.gerarCsv(ResultadoProcessamento resultado);
String ResumoTextoService.gerar(ResultadoProcessamento resultado);
```

`Analisar` não escreve artefatos. `Corrigir` nunca recebe componentes Swing e nunca sobrescreve a entrada.

## 4. Fluxo de dados

1. A UI valida apenas seleção/extensão e inicia um `SwingWorker`.
2. O reader preserva os 800 caracteres e registra metadados físicos.
3. O parser valida os campos definidos no requisito.
4. A análise percorre detalhes em ordem e registra primeira ocorrência e excedentes com números físicos originais.
5. A correção cria novas linhas em memória: cabeçalho, detalhes preservados, rodapé.
6. Somente quantidades, segurança, soma e sequenciais são substituídos.
7. O writer cria temporário no mesmo diretório, força Windows-1252/CRLF e move atomicamente.
8. O validador reabre o arquivo por caminho e recalcula tudo sem reutilizar a análise anterior.
9. O resultado imutável agrega análise, correções, validações, hashes, duração e caminhos.
10. TXT, CSV e UI consomem esse mesmo objeto.

## 5. Layout posicional imutável

| Campo | Posições humanas | Índices Java |
|---|---:|---:|
| Quantidade de detalhes do cabeçalho | 29–33 | `[28, 33)` |
| Quantidade de indicações do cabeçalho | 39–43 | `[38, 43)` |
| Segurança do rodapé | 13–17 | `[12, 17)` |
| Soma do rodapé | 18–35 | `[17, 35)` |
| Valor do detalhe | 579–592 | `[578, 592)` |
| Protocolo do detalhe | 593–606 | `[592, 606)` |
| Sequencial físico | 796–800 | `[795, 800)` |

O tipo está no primeiro caractere. Nenhuma posição adicional de campo cartorário será criada. Como o layout fornecido não define “controle do devedor”, o modelo e os relatórios manterão essa coluna vazia.

## 6. Erros, consistência e segurança

- Erros de estrutura impedem correção antes de qualquer saída.
- A análise expõe divergências de controles e sequenciais; erros que tornam campos obrigatórios ilegíveis são exceções claras.
- O plano de saída usa o nome sugerido e, se qualquer destino estiver ocupado, acrescenta o mesmo sufixo `_2`, `_3` etc. aos quatro arquivos.
- Logs são gravados em `Log`, CSV em `Relatorios/CSV` e TXT em `Relatorios/TXT`, todos sob a mesma pasta-base usada pelo histórico.
- Relatórios e configuração também usam escrita atômica.
- Falha pós-gravação não produz diálogo de sucesso; o erro aponta o log.
- A EDT apenas atualiza componentes e exibe diálogos.
- Detalhes técnicos e stack traces permanecem no log.

## 7. Testes e estabilidade da API

Os testes cobrem parser, duplicidades, ordem, grupos, BigInteger, campos, sequenciais, bytes, erros e validação independente. O teste de aceitação usa cópias em `src/test/resources/fixtures`; nunca altera os arquivos externos. As assinaturas acima são consideradas estáveis após cobertura.

## 8. Empacotamento

O Maven compila com `release 21`, executa JUnit 5 via Surefire, cria `CorretorSipat-<versão>-jar-with-dependencies.jar` e, no perfil `windows-package`, executa `jpackage --type app-image` em `dist`. O build padrão `mvn clean test package` permanece portátil e testável; no Windows, `mvn -Pwindows-package clean package` gera a aplicação empacotada.
