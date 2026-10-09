# Corretor SIPAT

Aplicação desktop em Java 21 para analisar e corrigir arquivos posicionais SIPAT, remover protocolos duplicados e recalcular controles com validação pós-gravação.

<div align="center">
  <img src="assets/corretor-sipat.png" alt="Infográfico do Corretor SIPAT com visão geral, funcionalidades, fluxo de processamento e tecnologias" width="100%">
</div>

## Visão geral

O Corretor SIPAT recebe um arquivo SIPAT posicional, preserva a primeira ocorrência física de cada protocolo e remove as ocorrências posteriores. Ao final, gera uma cópia corrigida, relatórios e log, sem alterar o arquivo de origem.

O processamento mantém o formato físico do arquivo e valida novamente a saída depois da gravação, antes de apresentá-la como concluída.

## Funcionalidades

- tabela **Duplicidades encontradas** com as quatro colunas centralizadas;
- seleção de arquivo pelo diálogo, histórico local ou arrastar e soltar;
- análise prévia da estrutura, dos indicadores e dos protocolos duplicados;
- preservação da primeira ocorrência de cada protocolo e remoção das posteriores;
- recálculo de quantidade, somatório de segurança, soma financeira e sequenciais;
- geração de SIP corrigido, relatório TXT, relatório CSV e log técnico;
- validação independente do arquivo produzido;
- histórico local de processamentos, com consulta, exclusão individual e limpeza confirmada;
- interface Swing com tema claro ou escuro do Windows, progresso e recursos de acessibilidade;
- janela de conclusão com resumo, linhas removidas, correções estruturais e atalhos para os arquivos gerados.

## Interface, histórico e organização

- Ao iniciar, nenhum arquivo ou resultado fica selecionado: os controles dependentes de seleção permanecem indisponíveis até que um arquivo seja escolhido. A área de arquivo apresenta a mensagem: **“Arraste e solte o arquivo SIPAT aqui ou clique em ‘Selecionar’”**.
- A tabela **Duplicidades encontradas** apresenta protocolo, linha preservada, linhas removidas e total de ocorrências com as informações centralizadas para facilitar a conferência.
- A janela **Processamento concluído** é redimensionável e mantém o resumo, as correções, as linhas removidas e os atalhos para os arquivos gerados acessíveis em diferentes tamanhos de tela.
- A janela **Histórico de arquivos** permite consultar e selecionar registros de processamentos anteriores. Seus botões possuem ícones, textos de apoio e ficam alinhados lado a lado, com espaçamento uniforme mesmo ao redimensionar a janela.
- No histórico, é possível excluir um registro individual ou limpar todos os registros, sempre com confirmação. Quando não há registros, a janela exibe a mensagem **“Não existem arquivos no histórico.”**.
- A aplicação organiza os dados persistidos nas pastas `Historico`, `Log`, `Relatorios/CSV` e `Relatorios/TXT`. As pastas necessárias são criadas automaticamente.
- Antes e depois da gravação, o SIP passa por validações estruturais, incluindo tamanho fixo dos registros, tipos de registro, controles, sequenciais e integridade da saída.

## Fluxo de processamento

```text
Arquivo SIPAT
    │
    ▼
Leitura, validação estrutural e análise de protocolos
    │
    ▼
Preservação da primeira ocorrência + registro das duplicidades removidas
    │
    ▼
Recálculo de controles, soma e sequenciais
    │
    ▼
Gravação do SIP corrigido + TXT + CSV + log
    │
    ▼
Releitura e validação independente da saída
```

## Tecnologias

- Java 21;
- Java Swing;
- FlatLaf;
- Apache Maven;
- JUnit 5;
- `jpackage` para a imagem de aplicação Windows.

## Estrutura principal

```text
src/main/java/com/corretorsipat/
├── config/    # preferências locais da interface
├── history/   # histórico de processamentos
├── io/        # leitura, gravação e diretórios de saída
├── log/       # log técnico de cada processamento
├── model/     # modelos imutáveis do domínio
├── parser/    # layout posicional e interpretação SIPAT
├── report/    # geração de TXT, CSV e resumo textual
├── service/   # análise, correção e validação pós-gravação
├── theme/     # integração com o tema do Windows
├── ui/        # janelas, painéis, tabelas e ícones Swing
└── util/      # formatação, hash e integração com o desktop
```

Os contratos, o fluxo de dados e as decisões técnicas estão detalhados em [docs/ARQUITETURA.md](docs/ARQUITETURA.md).

## Requisitos

- JDK 21;
- Apache Maven;
- Windows 10 ou 11;
- `jpackage` disponível no JDK para gerar a imagem de aplicação Windows;
- IntelliJ IDEA 2026.2.3, caso a execução seja feita pela IDE.

## Testes, compilação e execução

Execute a suíte automatizada e gere os JARs:

```powershell
mvn clean test package
```

O comando gera em `target/` o JAR comum e o JAR com dependências. Para iniciar a aplicação pelo terminal:

```powershell
java -jar target\CorretorSipat-1.0.0-jar-with-dependencies.jar
```

Para gerar a imagem da aplicação Windows com runtime próprio:

```powershell
mvn -Pwindows-package clean package
```

A imagem é criada em `dist\CorretorSipat`.

## Uso básico

1. Arraste um arquivo `.SIP` para a área **Arquivo SIPAT** ou clique em **Selecionar**.
2. Use **Analisar** para consultar estrutura, indicadores e duplicidades sem gravar arquivos.
3. Use **Corrigir arquivo** para gerar a cópia corrigida e os artefatos auxiliares.
4. Consulte a janela **Processamento concluído** para revisar o resumo, as linhas removidas e os caminhos gerados.
5. Use **Limpar** para restaurar apenas a sessão atual da interface, sem apagar histórico, relatórios, logs ou arquivos gerados.

## Arquivos gerados

Para um arquivo de entrada `arquivo.SIP`, a aplicação mantém o SIP corrigido ao lado da entrada e usa a pasta-base da aplicação para os demais artefatos:

```text
<pasta-da-entrada>/arquivo_SEM_DUPLICADOS.SIP
Historico/historico.txt
Log/arquivo_PROCESSAMENTO.log
Relatorios/TXT/arquivo_RELATORIO.txt
Relatorios/CSV/arquivo_LINHAS_REMOVIDAS.csv
```

As pastas necessárias são criadas automaticamente. Se qualquer destino do mesmo processamento já existir, todos os quatro artefatos recebem o mesmo sufixo numérico (`_2`, `_3` e assim por diante), sem sobrescrita silenciosa.

## Garantias de integridade

- cada registro produzido possui exatamente 800 caracteres;
- o cabeçalho é tipo `0`, os detalhes são tipo `1` e o rodapé é tipo `9`;
- a saída usa Windows-1252 sem BOM, CRLF e quebra de linha final;
- o arquivo original não é sobrescrito;
- somente os controles previstos são atualizados: quantidades, segurança, soma e sequenciais;
- os protocolos duplicados são avaliados na ordem física do arquivo;
- a saída é reaberta e validada de forma independente após a gravação;
- a soma é calculada com `BigInteger`.

## Escopo

O Corretor SIPAT implementa o layout posicional e as regras tratadas pelo código. Antes de usar um arquivo gerado em um fluxo externo, revise o relatório e execute as validações exigidas pelo sistema de destino.
