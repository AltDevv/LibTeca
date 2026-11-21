# LibTeca

Sistema de biblioteca com acervo físico e digital, reservas por exemplar e empréstimos com prazo de devolução. Backend Java 17 + Spring Boot; frontend responsivo em HTML, CSS e JavaScript, sem framework.

## Começar

Instale um JDK 17 ou superior. O Maven Wrapper acompanha o projeto e baixa as dependências na primeira execução.

No PowerShell, a partir da raiz:

```powershell
Copy-Item backend/src/main/resources/application-example.properties backend/src/main/resources/application.properties
cd backend
.\mvnw.cmd spring-boot:run
```

Copie a configuração de exemplo somente em uma instalação nova; não substitua a configuração local existente. Abra http://localhost:8080. O backend serve também o frontend; não é necessário outro servidor nem npm.

## Primeiro acesso

1. Abra a tela **Configurar biblioteca** e escolha **SQLite** ou **MySQL**. SQLite cria um arquivo local; MySQL pede servidor, porta, nome do banco, usuário e senha, e exige o modelo atual do Workbench já importado.
2. Copie o código do arquivo `backend/data/instalacao.codigo`, gerado ao iniciar. Ele vale por 30 minutos; reinicie o backend para renová-lo. Clique em **Verificar e continuar**. O sistema verifica a conexão, salva a escolha e reinicia automaticamente.
3. Escolha o nome da biblioteca (será o título da aba, o nome no menu e no catálogo). O site é opcional: informar uma URL não registra domínio nem publica o sistema.
4. Informe seu nome, email e uma senha de 8 a 60 caracteres. O código informado na etapa anterior permanece preenchido.
5. Confirme. A conta ADMIN e o funcionário correspondente são criados juntos. Não existe senha padrão.

A escolha fica em `backend/data/banco.properties`, incluindo as credenciais MySQL quando usadas; esse arquivo é local e ignorado pelo Git. O SQLite criado pela tela fica em `backend/data/libteca.db`. Para outro diretório, defina `LIBTECA_CONFIG_DIR` antes de iniciar. Nas próximas execuções, o sistema reutiliza a escolha. Uma instalação com perfil `mysql` ou `sqlite` definido explicitamente continua usando essa configuração, sem abrir a seleção inicial. Essa tela não transfere dados entre bancos.

O código só é fornecido no servidor e é invalidado após a configuração. A instalação não pode ser repetida pela API. Se o banco já tiver administrador, entre com essa conta para configurar a identidade, preservando os registros. Depois, o administrador pode alterar nome/site em **Minha biblioteca**.

Cada instalação atende uma biblioteca com seu próprio banco. A tabela adicional `configuracao_biblioteca` guarda a identidade e o estado da configuração. Isso não implementa múltiplas bibliotecas no mesmo banco.

O catálogo vazio mostra três cartões **Livro exemplo**, sem registros no banco nem operações de empréstimo. O administrador pode removê-los nesse navegador. O cadastro do primeiro livro real desativa os exemplos permanentemente na instalação, mesmo se o livro for apagado depois.

## Configuração manual do MySQL

Crie o banco com o SQL exportado do modelo atual do Workbench. O backend valida as tabelas do acervo; não migra um modelo antigo nem apaga dados. A tabela de configuração é criada automaticamente se estiver ausente.

```powershell
$env:DB_PROFILE='mysql'
$env:LIBTECA_DB_URL='jdbc:mysql://localhost:3306/libteca'
$env:LIBTECA_DB_USERNAME='seu_usuario'
$env:LIBTECA_DB_PASSWORD='sua_senha'
.\mvnw.cmd spring-boot:run
```

Também são aceitas as variáveis padrão `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`. SQLite cria sua estrutura por SQL próprio. Os perfis são alternativas, sem sincronização entre bancos.

## Acesso e arquivos

- Leitores: catálogo, conta, telefones, reservas e empréstimos próprios; leitura digital enquanto o empréstimo estiver vigente.
- Funcionários: cadastro do acervo e leitores, circulação e devoluções.
- Livros e exemplares têm edição com os dados preenchidos. Exemplares reservados ou emprestados não podem ser editados; exemplares com histórico não podem trocar de livro ou formato.
- Administrador: mesmas operações, gestão da equipe e identidade da biblioteca.
- Login por email/senha, BCrypt, sessão HttpOnly e proteção CSRF. O funcionário responsável é obtido pelo backend a partir da sessão.
- Upload de capas PNG/JPG e livros PDF de até 20 MB. Arquivos ficam em `backend/data/arquivos` por padrão. Capas são públicas; PDFs passam pela autorização do empréstimo.
- Não há opção de empréstimo sem prazo nem campo `permiteDownload`. A leitura no navegador não constitui DRM e não impede tecnicamente que o conteúdo seja salvo pelo leitor.

Para demonstração, um túnel para a porta 8080 entrega o frontend e a API pelo mesmo endereço. Em hospedagem pública, configure o domínio, HTTPS e cookie de sessão seguro (`SERVER_SERVLET_SESSION_COOKIE_SECURE=true`). O banco não precisa ficar público. Mantenha o backend e o banco disponíveis enquanto houver acesso.

## Organização

A pasta `frontEnd` segue a organização em `config`, `controller`, `dto`, `entity`, `enums`, `filter`, `handler`, `mapper`, `repository`, `service` e `specification`, além de `telas`, `css` e `assets`. O Maven copia esses arquivos para `backend/target/classes/static` durante `process-resources` e os inclui no JAR, sem registrar `frontEnd` como raiz de recursos do módulo Java. Após editar o frontend, reinicie `spring-boot:run` ou gere outro pacote para copiar os recursos.

## Verificação

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd package -DskipTests
```

Os testes usam bancos isolados H2/SQLite. A suíte MySQL é opcional: configure `LIBTECA_MYSQL_TEST_URL`, `LIBTECA_MYSQL_TEST_USER` e `LIBTECA_MYSQL_TEST_PASSWORD` para um banco exclusivamente de testes.

Arquivos locais de configuração, bancos, código de instalação e uploads estão ignorados pelo Git. Nenhum acervo ou usuário de demonstração é inserido automaticamente.

Veja [o guia do backend](backend/GUIA_BACKEND.md) e [a organização do frontend](frontEnd/README.md).

Projeto de estudos de Vítor Nathan Cavalcanta Damasio.
