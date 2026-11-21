# LibTeca

Sistema de biblioteca com acervo físico e digital, reservas e empréstimos com prazo de devolução. Leitores consultam o catálogo e solicitam livros; funcionários e administradores gerenciam o acervo, as entregas e as devoluções. O sistema não realiza compras ou pagamentos.

O frontend é responsivo, feito em HTML, CSS e JavaScript, sem framework. O backend Java com Spring Boot serve tanto as telas quanto a API: basta iniciar o backend e abrir o navegador.

**Baixou o projeto para experimentar? Comece com SQLite seguindo o passo a passo abaixo.** Não é necessário instalar um servidor de banco nessa opção. Para MySQL, há uma preparação adicional descrita neste documento.

## 1. O que baixar

Para começar, instale **Java JDK 17** e use um navegador atualizado. Se escolher MySQL, instale também **MySQL Server 8.4**. Se escolher SQLite, o próprio projeto prepara o banco.

| Programa | Versão para instalar |
| --- | --- |
| Java JDK | **17** |
| MySQL Server — para quem escolher MySQL | **8.4** |

Você pode usar o IntelliJ IDEA ou VS Code para abrir o código e o MySQL Workbench para administrar o MySQL. O projeto não exige uma versão específica dessas ferramentas. Também é possível executar tudo pelo terminal.

**Não precisa baixar o Maven separadamente:** ele já vem configurado no projeto. Ao executar `mvnw.cmd`, a versão **3.9.16** é baixada automaticamente, junto com as bibliotecas abaixo.

### Versões usadas pelo código

Esta lista é apenas para consulta. Você não precisa instalar essas bibliotecas uma por uma nem mudar suas versões: o `pom.xml` já cuida disso.

| Biblioteca | Versão |
| --- | --- |
| Spring Boot | 4.1.0 |
| Lombok | 1.18.46 |
| Springdoc / Swagger UI | 3.2.0 |
| MySQL Connector/J — conexão do Java com MySQL | 9.7.0 |
| SQLite JDBC — banco SQLite usado pelo projeto | 3.53.2.0 |
| Playwright Java — testes do frontend | 1.64.0 |

As demais dependências, como Spring Security, Spring Data JPA, Hibernate e JUnit, têm suas versões definidas pelo Spring Boot. Consulte [pom.xml](backend/pom.xml) e [maven-wrapper.properties](backend/.mvn/wrapper/maven-wrapper.properties).

O frontend usa **HTML, CSS e JavaScript**. Para abrir o site, basta o navegador; não é necessário instalar Flutter ou Node.js.

**MySQL Server é o banco; Workbench é a ferramenta visual para administrá-lo.** Instalar somente o Workbench não basta para usar MySQL. A versão do Connector/J na tabela é a biblioteca Java, não o servidor que você deve baixar.

## 2. Baixar o projeto completo

No GitHub, use **Code → Download ZIP** e extraia o arquivo antes de executar. Se preferir Git, copie a URL HTTPS exibida em **Code** e use:

```powershell
# Substitua a URL abaixo pela URL real do repositório.
git clone URL_DO_REPOSITORIO libTeca
cd libTeca
```

Abra o PowerShell na pasta extraída/clonada. Ela deve conter, pelo menos:

```text
libTeca/
├── backend/
│   ├── .mvn/wrapper/
│   ├── src/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontEnd/
├── banco/
│   └── schema-mysql.sql
├── scripts/
└── README.md
```

**Mantenha `backend` e `frontEnd` lado a lado e com esses nomes.** O Maven busca o frontend em `../frontEnd` e o inclui no pacote do sistema. Baixar somente `backend/src` ou mover o frontend impede a montagem completa.

## 3. Preparar o Java e a configuração local

Instale o JDK 17 e abra um novo terminal. Confira:

```powershell
java -version
javac -version
```

Os dois comandos devem funcionar. Se aparecer Java 8/11, comando não encontrado ou apenas o `java` funcionar, ajuste a instalação do JDK e o `Path` do Windows. `JAVA_HOME` deve apontar para a pasta do JDK, sem o trecho `bin`. Exemplo para a sessão atual, substituindo o caminho pela sua instalação:

```powershell
$env:JAVA_HOME = 'C:\caminho\do\seu\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Na **raiz do projeto**, crie a configuração local a partir do modelo. O comando abaixo preserva o arquivo se ele já existir:

```powershell
if (-not (Test-Path 'backend/src/main/resources/application.properties')) {
    Copy-Item 'backend/src/main/resources/application-example.properties' 'backend/src/main/resources/application.properties'
}
cd backend
.\mvnw.cmd -version
```

Confira se a saída mostra Maven 3.9.16 e o Java escolhido. O Java do Maven pode ser diferente do Java configurado na IDE; essa verificação ajuda a encontrar o problema.

O arquivo `application.properties` é local e não acompanha o Git. O modelo usa a porta **8080** e abre a seleção de banco na primeira instalação. Não é necessário escrever credenciais nesse arquivo para usar a configuração pela tela.

## 4. Iniciar e configurar com SQLite

Com o terminal dentro de **`backend`**, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Na primeira execução, o Maven baixa as bibliotecas necessárias; isso exige internet e pode demorar. Aguarde a mensagem de aplicação iniciada e do servidor na porta 8080. O terminal permanece ocupado enquanto o servidor estiver ligado — isso é esperado. `BUILD FAILURE` ou `APPLICATION FAILED TO START` indicam uma falha, não uma aplicação pronta.

1. Abra [http://localhost:8080](http://localhost:8080) no navegador. Não abra o HTML pelo explorador de arquivos nem por um servidor separado.
2. Na tela inicial, escolha **SQLite**.
3. Abra o arquivo **`backend/data/instalacao.codigo`**, copie seu conteúdo para o campo de código e clique em **Verificar e continuar**. Esse arquivo é gerado ao iniciar; o código vale por 30 minutos. Para renová-lo, pare com `Ctrl+C` e inicie novamente.
4. O sistema salva o banco escolhido e reinicia automaticamente. Aguarde a próxima etapa.
5. Preencha o nome da biblioteca, seu nome, email e senha de 8 a 60 caracteres. O endereço do site é opcional e pode ficar vazio para uso local. O código da etapa anterior permanece preenchido.
6. Conclua a configuração. Sua conta será o administrador; o registro de funcionário correspondente também será criado. **Não há login ou senha padrão.**

O nome escolhido aparece no título da aba, no menu e no catálogo. Depois, o administrador pode alterá-lo em **Minha biblioteca**.

Para consultar o código pelo terminal, abra **outro PowerShell** dentro de `backend`:

```powershell
Get-Content .\data\instalacao.codigo
```

O SQLite cria as tabelas e salva os dados em `backend/data/libteca.db`. A escolha do banco fica em `backend/data/banco.properties`. Nas próximas execuções, basta entrar na pasta `backend`, executar novamente `spring-boot:run` e acessar o mesmo endereço. Não repita a instalação nem apague a pasta `data`.

**O projeto base começa sem livros e sem leitores cadastrados.** Os três cartões “Livro exemplo” são apenas visuais; não representam registros disponíveis para empréstimo. O cadastro do primeiro livro real desativa esses exemplos permanentemente na instalação. Capas, livros e contas usados em apresentações não são inseridos automaticamente em outra instalação.

## 5. Usar MySQL em vez de SQLite

Para uma instalação nova com MySQL, prepare o banco **antes de selecioná-lo na tela**. O backend valida as tabelas do acervo; ele não cria toda a estrutura MySQL a partir das entidades.

1. Instale e inicie o **MySQL Server 8.4**. Anote host, porta, usuário e senha definidos na instalação. Recomendo a utilização do Docker para facilitar, e caso use, lembre-se que na instalação do Workbench, baixe apenas a interface visual dele, para consultar o MySQL presente no Docker. Em uma instalação local, o host normalmente é `localhost` e a porta `3306`.
2. Conecte ao servidor pelo Workbench ou outro cliente MySQL. A senha do banco é a definida no MySQL, não a senha que você criará para entrar no LibTeca.
3. Em uma query, crie e selecione um banco novo:

   ```sql
   CREATE DATABASE libteca CHARACTER SET utf8mb4;
   USE libteca;
   ```

4. Abra [banco/schema-mysql.sql](banco/schema-mysql.sql), selecione o schema `libteca` como padrão na aba em que vai executar e rode o arquivo inteiro. Ele cria as tabelas, chaves e índices, sem inserir livros ou usuários. **Execute uma única vez em um banco vazio.** Se já houver um banco `libteca`, preserve seus dados e escolha outro nome para uma instalação independente. Esse script não é uma migração.
5. O usuário MySQL usado pela aplicação precisa de permissões de leitura e escrita. Atualizações de instalações antigas podem exigir `ALTER` para adicionar colunas e `DROP` para remover as duas tabelas auxiliares descontinuadas, após backup local. Essas permissões se aplicam somente ao schema do LibTeca. Use uma conta dedicada à aplicação quando disponibilizar o sistema.
6. Inicie o backend como no passo anterior e escolha **MySQL** na tela. Informe host, porta, nome do banco, usuário, senha e o código de instalação.
7. Continue com a criação da biblioteca e do administrador.

O arquivo de instalação inclui as 12 tabelas do domínio, com `emprestimo.prazo_escolhido` e `livro.limite_reservas`. A identidade da biblioteca fica em um arquivo local; não há tabela de configuração nem de pedidos de funcionários. Alterações mais amplas de um modelo antigo precisam de migração própria; não use `ddl-auto=create` para tentar corrigir um banco com dados.

### Configuração manual por variáveis de ambiente

Esta é uma alternativa à seleção inicial pela tela. Com as tabelas já importadas e o terminal em `backend`:

```powershell
$env:DB_PROFILE = 'mysql'
$env:LIBTECA_DB_URL = 'jdbc:mysql://localhost:3306/libteca'
$env:LIBTECA_DB_USERNAME = 'seu_usuario'
$env:LIBTECA_DB_PASSWORD = 'sua_senha'
.\mvnw.cmd spring-boot:run
```

Substitua os valores pelos seus dados reais. Essas variáveis valem para a sessão atual do PowerShell. Os perfis também aceitam as variáveis padrão `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`.

Uma escolha já salva em `data/banco.properties` pode prevalecer sobre credenciais externas quando o mesmo perfil está ativo. Use um único método de configuração por instalação. Trocar o perfil ou o endereço não copia dados do SQLite para MySQL, nem do MySQL para SQLite.

## 6. Abrir e executar no IntelliJ IDEA

**Abra a pasta inteira `libTeca` para visualizar o backend e o frontend juntos.** Você continuará com um único projeto. O Maven é a ferramenta que baixa as bibliotecas, compila o código e inicia o sistema; suas configurações já estão no arquivo `backend/pom.xml`.

1. Baixe e extraia o projeto completo, conforme a seção 2.
2. No IntelliJ, clique em **File → Open** e selecione a pasta **`libTeca`**, que contém `backend` e `frontEnd`.
3. Na lista de arquivos, abra a pasta `backend`, clique com o botão direito em **`pom.xml`** e escolha **Add as Maven Project**. Se o IntelliJ já reconheceu o Maven automaticamente, essa etapa já está feita.
4. Em **File → Project Structure → Project → SDK**, selecione o **JDK 17** instalado no computador.
5. Em **Settings → Build, Execution, Deployment → Build Tools → Maven**, selecione **Maven Wrapper** como Maven utilizado, quando essa opção estiver disponível. Ele usa a versão definida pelo projeto. Confira também se o Java do importador e do runner do Maven está configurado para o JDK 17.
6. Aguarde o IntelliJ carregar as bibliotecas. Se necessário, clique em **Reload All Maven Projects** na janela Maven.
7. Dentro de `backend/src/main/resources`, copie `application-example.properties` e renomeie a cópia para **`application.properties`**. Faça isso apenas se `application.properties` ainda não existir. É o mesmo arquivo preparado na seção 3.
8. Abra a aba **Terminal** do IntelliJ. Se o caminho mostrado terminar em `libTeca`, execute `cd backend`. Se já terminar em `backend`, não precisa entrar novamente.
9. Execute:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

10. Aguarde o servidor iniciar e abra **[http://localhost:8080](http://localhost:8080)**. Na primeira vez, siga a configuração da seção 4 para SQLite ou da seção 5 para MySQL.

O comando acima baixa o Maven e as bibliotecas quando necessário, compila o backend, prepara as telas e inicia o LibTeca. **O frontend já é servido pelo backend; não há outro comando para iniciá-lo.** Mantenha o terminal aberto enquanto estiver usando o sistema.

### Depois: executar pelo botão Run

Você também pode abrir a classe `BackendApplication`, em `backend/src/main/java/com/libteca`, e usar o botão Run. Na configuração dessa execução, selecione o JDK 17 e defina **Working directory** como o caminho completo da pasta `backend`, por exemplo:

```text
C:\Users\SeuNome\IdeaProjects\libTeca\backend
```

Esse campo significa “pasta de onde o programa será executado”. Usar sempre `backend` mantém o banco local e os logs nos mesmos locais: `backend/data` e `backend/logs`.

Não execute pelo terminal e pelo botão Run ao mesmo tempo na mesma porta. Se o Maven compilar, mas a IDE marcar métodos do Lombok como inexistentes, confira o suporte ao Lombok e o processamento de anotações da IDE.

Não é necessário usar **New Project**: o LibTeca já vem pronto como projeto Maven. Veja também a [documentação de projetos Maven no IntelliJ](https://www.jetbrains.com/help/idea/maven-support.html).

## 7. Parar, iniciar novamente e gerar um pacote

Pare o servidor com `Ctrl+C` no terminal, ou com o botão Stop da IDE. Os registros salvos no banco permanecem. Para iniciar novamente, execute o mesmo comando, sempre a partir de `backend`.

Para gerar um JAR com backend e frontend juntos, dentro de `backend`:

```powershell
.\mvnw.cmd package -DskipTests
java -jar .\target\backend-0.0.1-SNAPSHOT.jar
```

`-DskipTests` pula a execução dos testes nessa montagem; não significa que eles foram validados. O JAR inclui as telas, mas não inclui a pasta `data`, o banco local nem os uploads. **O `application.properties` é empacotado:** mantenha senhas fora dele ao distribuir o JAR e use a configuração externa do ambiente. Execute apenas uma instância por porta. Ao transferir para outro computador, mantenha uma pasta de execução fixa e configure o banco e o armazenamento desse ambiente.

## 8. Começar a usar

**Administrador:** entre com a conta criada na instalação. Em **Cadastros**, adicione autores, categorias e editoras. Depois, cadastre o livro em **Livros**, envie a capa se desejar e escolha se ele aceita reservas e qual é o limite. Em **Exemplares**, adicione as unidades físicas com seus patrimônios ou um exemplar virtual com o PDF. Um livro sem exemplares ainda pode aparecer no catálogo, mas não pode ser emprestado.

**Equipe:** o administrador cadastra a conta da pessoa em **Usuários** e a vincula em **Equipe**. Somente o administrador pode adicionar, desativar ou remover funcionários. Funcionários cuidam do acervo, dos leitores, das entregas e das devoluções; não enviam pedidos para incluir outros funcionários.

**Leitor:** crie sua conta, entre no catálogo, abra um livro disponível e clique em **Emprestar** para o físico ou **Começar leitura** para o virtual. Escolha **1 semana, 1 mês ou 2 meses** e confirme. No físico, o pedido aguarda a entrega: um funcionário ou administrador confirma em **Gestão → Empréstimos**, e só então começa o prazo. No virtual, o empréstimo começa imediatamente e a leitura fica na própria tela. Um mês e dois meses seguem o calendário, não uma quantidade fixa de dias.

Quando faltar exemplar do formato desejado e o livro aceitar reservas, aparece **Reservar**, respeitando o limite cadastrado. O leitor acompanha seus pedidos em **Meus empréstimos** e **Minhas reservas**. A devolução física é registrada pela equipe; o leitor pode devolver o digital pela sua conta.

Para mudar o nome ou endereço do site, o administrador usa **Minha biblioteca**. Nessa tela também pode transferir a administração para outro funcionário ativo ou apagar os dados da instalação, mediante confirmação. Transferir a administração preserva o banco e o endereço do site.

## 9. Testes separados

Na **raiz do repositório**, os comandos Maven abaixo executam as suítes separadamente. `Push-Location` entra temporariamente na pasta correta e `Pop-Location` retorna à raiz:

```powershell
# Backend: JUnit.
Push-Location backend
.\mvnw.cmd test
Pop-Location

# Frontend: Playwright, com servidor e banco isolados para os testes.
Push-Location backend
.\mvnw.cmd -Pfront-playwright test
Pop-Location
```

Os testes usam bancos isolados H2/SQLite. Para acompanhar o navegador, abrir relatórios ou escolher um cenário, consulte o [manual de testes do frontend](frontEnd/TESTES_PLAYWRIGHT.md). Os detalhes da suíte de backend e dos testes opcionais com MySQL ficam no [guia do backend](backend/GUIA_BACKEND.md#testes).

## 10. Problemas comuns

| Problema | O que conferir |
| --- | --- |
| `java` ou `javac` não reconhecido | Instale o JDK, ajuste `JAVA_HOME`/`Path` e abra outro terminal |
| `release version 17 not supported` | Execute `.\mvnw.cmd -version`: o Maven está usando um JDK antigo |
| `.\mvnw.cmd` não encontrado | Entre em `backend`; confira se baixou também `mvnw.cmd` e `.mvn/wrapper` |
| Maven não encontra um projeto / falta `pom.xml` | Execute dentro de `backend`, onde fica o `pom.xml` |
| Dependências não baixam / `Could not resolve` | Confira internet, proxy, modo offline da IDE e a dependência indicada no erro. Após corrigir a conexão, tente `.\mvnw.cmd -U spring-boot:run` |
| Primeira execução demorada | Acompanhe os downloads no terminal. Aguarde a mensagem de aplicação iniciada; se houver falha, leia o primeiro erro relevante e o trecho `Caused by` |
| Porta 8080 ocupada | Pare outra instância do LibTeca ou use `$env:SERVER_PORT = '8081'` antes de iniciar. Acesse então `http://localhost:8081` |
| Navegador não abre o site | Confirme se o backend iniciou, a porta utilizada e se o terminal continua aberto. Não abra `index.html` diretamente |
| Tela antiga após mudar o frontend | Reinicie `spring-boot:run` para copiar os recursos ou gere outro JAR; depois recarregue o navegador |
| MySQL: conexão recusada | Verifique se o **servidor MySQL** está iniciado e se host/porta estão corretos. Abrir o Workbench não inicia necessariamente o servidor |
| MySQL: `Access denied` | Confira usuário, senha e permissões no schema usado |
| MySQL: tabela/coluna ausente | Em uma instalação nova, importe `banco/schema-mysql.sql` no schema correto antes de iniciar. Um banco antigo precisa de atualização compatível |
| Código de instalação inválido/expirado | Reinicie, abra o arquivo atual em `backend/data/instalacao.codigo` e copie o novo código |
| Não aparece a escolha de banco | Pode haver `data/banco.properties` ou perfil explícito (`DB_PROFILE`, `SPRING_PROFILES_ACTIVE`, argumentos da IDE). A seleção é para uma instalação nova |
| Parece que os dados sumiram ao iniciar pela IDE | Confira o Working directory. Executar na raiz em vez de `backend` muda o caminho relativo de `data`; não apague os arquivos existentes |

O log padrão fica em `backend/logs/libteca.log` quando o processo é iniciado dentro de `backend`. Ao pedir ajuda, informe o comando executado, a saída de `java -version` e `.\mvnw.cmd -version`, o banco escolhido e o erro do terminal. Remova senhas, códigos de instalação e dados pessoais antes de compartilhar.

## 11. Dados, configuração e publicação

- **Leitor:** catálogo, conta, telefones, reservas e empréstimos próprios; acesso ao PDF enquanto o empréstimo digital estiver vigente.
- **Funcionário:** acervo, leitores, entregas e devoluções; não cadastra nem exclui funcionários.
- **Administrador:** mesmas operações e gestão da equipe e da biblioteca.
- **Prazos:** apenas 1 semana, 1 mês ou 2 meses. A semana tem 7 dias; meses seguem o calendário. No físico solicitado pelo leitor, a contagem começa na entrega confirmada pela equipe. No digital, começa na criação do empréstimo.
- **Reservas:** respeitam disponibilidade e limite configurado por livro.
- **Uploads:** capas PNG/JPG e PDFs de até 20 MB; armazenamento padrão em `backend/data/arquivos`. Capas são públicas; PDFs exigem autorização do empréstimo. A leitura no navegador não constitui DRM.
- **Autenticação:** email/senha, hash BCrypt, sessão HttpOnly e proteção CSRF. Não há senha padrão compartilhada.

Empréstimos anteriores à seleção de prazo preservam suas datas. Solicitações físicas antigas sem `prazo_escolhido` mantêm a regra anterior de 14 dias após a entrega. As configurações de inicialização adicionam essa coluna quando ausente; isso não substitui uma migração completa de bancos antigos.

Cada instalação atende uma biblioteca com seu próprio banco. O backend salva o nome, o endereço do site e os indicadores de instalação/acervo em **`backend/data/biblioteca.properties`**. As alterações feitas em **Minha biblioteca** atualizam esse arquivo. As contas, livros, exemplares, reservas e empréstimos continuam no banco escolhido. Use uma instância do backend por instalação.

Ao atualizar uma instalação antiga, o backend transfere a configuração existente para o arquivo e remove as tabelas `configuracao_biblioteca` e `solicitacao_funcionario`, guardando cópias CSV em `backend/data/migracoes`. Se você já apagou a configuração com `DROP`, o sistema reconhece o administrador existente e mantém seu acesso. Como nome e site não podem ser reconstruídos do nada, confira-os em **Minha biblioteca**; sem uma cópia anterior, o nome inicial será “Minha biblioteca” e o site ficará vazio. Não é necessário reinstalar nem cadastrar outro administrador.

`localhost` funciona no computador que executa o sistema. Para outras pessoas acessarem pela internet, é necessário publicar o serviço ou configurar um túnel para sua porta. **Preencher “Endereço do site” só salva a URL; não publica o LibTeca.** Em hospedagem pública, configure domínio, HTTPS e cookie de sessão seguro (`SERVER_SERVLET_SESSION_COOKIE_SECURE=true`). O banco não precisa ficar exposto publicamente.

O Git ignora configurações locais, `data`, bancos, uploads, códigos de instalação, logs e arquivos gerados em `target`. Para preservar uma instalação, faça backup do banco **e da pasta `backend/data`**, que contém a identidade da biblioteca, a configuração da conexão e os arquivos enviados. Proteja esse backup, pois ele pode conter credenciais e dados pessoais. Copiar somente o código não copia o acervo nem a configuração.

`LIBTECA_CONFIG_DIR` muda o diretório da configuração de instalação e do banco criado pela tela; `LIBTECA_ARQUIVOS` muda o armazenamento de uploads; `LIBTECA_LOG_DIR` muda os logs. Defina caminhos consistentes entre as execuções.

## Organização e referências do projeto

O frontend segue as pastas `config`, `controller`, `dto`, `entity`, `enums`, `filter`, `handler`, `mapper`, `repository`, `service` e `specification`, além de `telas`, `css` e `assets`. O Maven copia esses recursos para `backend/target/classes/static` e os inclui no JAR.

Veja também [o guia do backend](backend/GUIA_BACKEND.md), [a documentação do frontend](frontEnd/README.md) e [o SQL de instalação MySQL](banco/schema-mysql.sql).
