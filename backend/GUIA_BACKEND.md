# Backend do LibTeca

Adaptação ao arquivo banco_LibTeca.mwb conferido em 12/11/2026. Foram mantidos Java, Spring Boot, Lombok, DTOs em records, mappers manuais, injeção por construtor e a divisão entre controller, dto, entity, enums, filter, handler, mapper, repository, service e specification.

## Modelo

- As 12 tabelas do modelo são representadas por 10 entidades. Livro usa conjuntos de autores e categorias, com as tabelas livro_autor e livro_categoria e suas chaves primárias compostas.
- Os nomes das tabelas e colunas são explícitos nas anotações. Os IDs BIGINT usam Long.
- Funcionário complementa um usuário. A mesma conta só pode ter um cadastro de funcionário. Cadastrar um funcionário promove USER para FUNCIONARIO; ADMIN é preservado.
- ADMIN é o nome correto do perfil de administrador. ADMIM continua aceito para não invalidar registros antigos.
- O funcionário do livro identifica o responsável pelo cadastro. Em reserva e empréstimo, funcionarioId pode ser nulo.
- capa é a referência da imagem do livro. caminhoArquivo é a referência do arquivo do exemplar virtual. O upload retorna essas referências; capas são servidas publicamente e PDFs somente mediante empréstimo vigente.
- Telefone mantém o UNIQUE global definido no modelo: um número não pode pertencer a dois cadastros.

## Empréstimos e reservas

O empréstimo aponta para um exemplar. Exemplares físicos e virtuais têm o mesmo ciclo:

1. Informar dataPrevistaDevolucao, igual ou posterior à data atual.
2. O backend grava esse prazo em data_expiracao; data_devolucao permanece NULL.
3. Quando o prazo passa, o status pode virar EXPIRADO. O exemplar continua emprestado.
4. A operação de devolver registra a data efetiva, marca DEVOLVIDO e libera o exemplar ou o destina à reserva pendente.

Não existem mais permiteDownload, possuiExpiracao, diasExpiracao, acessos ilimitados nem contadores de estoque em livro. A disponibilidade é calculada a partir dos exemplares. Uma devolução virtual também precisa ser registrada; o vencimento sozinho não devolve o exemplar.

A reserva é de um exemplar específico. Nesta implementação, há no máximo uma reserva em andamento por exemplar. Ela pode aguardar um empréstimo terminar ou a data de lançamento. Quando o exemplar fica livre e o livro já foi lançado, fica DISPONIVEL_PARA_RETIRADA. A retirada cria o empréstimo com o prazo informado e conclui a reserva.

Reservas em andamento precisam ser canceladas antes de excluídas. Empréstimos sem devolução não podem ser apagados. Exclusões que violam FKs são rejeitadas, preservando os registros relacionados.

## Exemplos de requisições

Use IDs que realmente existam. As datas abaixo são exemplos: ajuste o prazo à data em que executar.

POST /api/funcionarios

```json
{"usuarioId": 1, "dataAdmissao": "2026-11-12", "ativo": true}
```

POST /api/telefones

```json
{"usuarioId": 1, "numero": "11987654321"}
```

POST /api/livros

```json
{
  "titulo": "Meu livro",
  "autoresIds": [1, 2],
  "categoriasIds": [1],
  "editoraId": 1,
    "capa": "capas/meu-livro.jpg",
  "numeroDePaginas": 200,
  "dataLancamento": "2025-04-10"
}
```

POST /api/exemplares — físico

```json
{"livroId": 1, "tipo": "FISICO", "dataAquisicao": "2026-11-12", "codigoPatrimonio": "PAT-001", "caminhoArquivo": null}
```

POST /api/exemplares — virtual

```json
{"livroId": 1, "tipo": "VIRTUAL", "dataAquisicao": "2026-11-12", "codigoPatrimonio": null, "caminhoArquivo": "livros/meu-livro.pdf"}
```

POST /api/emprestimos

```json
{"exemplarId": 1, "usuarioId": 2, "dataPrevistaDevolucao": "2026-12-20"}
```

PATCH /api/emprestimos/1/devolver — sem corpo.

POST /api/reservas

```json
{"exemplarId": 1, "usuarioId": 2}
```

PATCH /api/reservas/1/retirar

```json
{"dataPrevistaDevolucao": "2026-12-20", "funcionarioId": 1}
```

PATCH /api/reservas/1/cancelar — sem corpo.

Funcionários, exemplares e telefones possuem GET na coleção e no ID, POST e DELETE na coleção e no ID. Funcionário também possui PUT /api/funcionarios/{id}, com o mesmo corpo do cadastro, para alterar admissão e ativo; não permite trocar o usuário.

Consultas adicionais:

- GET /api/exemplares/livro/{livroId}
- GET /api/telefones/usuario/{usuarioId}
- GET /api/livros/buscar?autor=Machado&categoria=Romance
- GET /api/livros/buscar?tipoLivro=FISICO&quantidadeMinima=1

O filtro de quantidade conta exemplares DISPONIVEL, e considera tipoLivro quando informado. As respostas do livro trazem listas autores e categorias. As respostas de empréstimo e reserva trazem exemplar, usuario, funcionarioId e status.

## Bancos e execução

MySQL usa o esquema do Workbench com ddl-auto=validate. A aplicação não transforma nem repovoa um banco antigo. Configure a conexão em application-mysql.properties ou nas variáveis padrão SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME e SPRING_DATASOURCE_PASSWORD. Use DB_PROFILE=mysql.

Para SQLite, use DB_PROFILE=sqlite e crie a pasta data no diretório de execução. O arquivo padrão é data/libteca.db. db/schema-sqlite.sql cria as tabelas ausentes com FKs, UNIQUEs e chaves compostas; ele não migra estruturas antigas. O SQLite usa uma conexão por instância para serializar as escritas locais. Os dois perfis são alternativas de armazenamento, não uma sincronização entre bancos.

O SQL explícito de SQLite evita depender da geração automática de identidades e FKs pelo Hibernate. As datas usam texto yyyy-MM-dd, configurado pelas propriedades date_class e date_string_format do [driver SQLite JDBC](https://github.com/xerial/sqlite-jdbc/blob/master/src/main/java/org/sqlite/SQLiteConfig.java).

Dentro da pasta backend:

```powershell
./mvnw.cmd spring-boot:run
```

application-example.properties contém a configuração básica para novos ambientes. A configuração local existente foi preservada e convertida para UTF-8 para corrigir a leitura do Maven.

## Autenticação, configuração e arquivos

O login consulta o usuário do banco e confere BCrypt. A sessão usa cookie HttpOnly, SameSite=Lax e CSRF nas alterações. Não há senha padrão nem JWT. Funcionários inativos não acessam a gestão. O backend obtém o funcionário responsável da sessão, sem confiar no ID enviado pelo cliente.

A configuração inicial cria ADMIN + Funcionario em uma transação, usando o código temporário local. `configuracao_biblioteca` é uma tabela adicional às 12 do domínio, criada por `db/configuracao-biblioteca.sql` nos dois bancos. Ela contém a identidade da instalação e indica se o primeiro livro já foi cadastrado. Não particiona os dados em múltiplas bibliotecas.

| Configuração | Função |
| --- | --- |
| ArquivoConfig | Diretório de armazenamento e limite de arquivos |
| CorsConfig | Origens explicitamente permitidas; mesma origem funciona sem liberação extra |
| JSONConfig | Serialização e rejeição de campos JSON desconhecidos |
| OpenApiConfig | Documentação da API em /swagger-ui.html, protegida para a equipe |
| SecurityConfig | Login, sessão, CSRF, BCrypt e autorização das rotas |
| InstalacaoConfig / InstalacaoInicialConfig | Geração, validade e invalidação do código de instalação |

POST /api/arquivos recebe multipart `arquivo` e `tipo` (capa ou livro), para a equipe. PNG/JPG/PDF são validados por formato e limite de tamanho. GET /capas/{nome} fornece capas; GET /api/arquivos/leitura/{emprestimoId} verifica dono, tipo virtual, prazo e devolução antes de fornecer o PDF. O diretório de PDFs não é exposto como recurso estático.

A leitura digital exige devolução e possui prazo de 14 dias quando iniciada pelo leitor. Funcionários informam o prazo nas operações de circulação. Exibir PDF não impede sua cópia no navegador.

## Testes

Iniciando pela pasta `backend`, os logs da aplicação ficam em `logs/libteca.log`. Para escolher outro diretório, defina `LIBTECA_LOG_DIR`. Os registros não são versionados; `.gitkeep` mantém a pasta vazia no Git. Saídas de comandos Maven podem ser direcionadas para essa mesma pasta, por exemplo: `./mvnw.cmd test *> logs/testes.log` no PowerShell.

```powershell
./mvnw.cmd test
```

Executa os fluxos em H2 e em um arquivo SQLite temporário. Para incluir MySQL, informe LIBTECA_MYSQL_TEST_URL, LIBTECA_MYSQL_TEST_USER e LIBTECA_MYSQL_TEST_PASSWORD apontando exclusivamente para um banco vazio de testes. O inicializador cria as tabelas de schema-modelo.sql; esse SQL foi extraído do .mwb, preservando colunas, PKs, UNIQUEs e FKs. Índices auxiliares são visíveis na estrutura de teste.

Os testes cobrem criação e relações, filtros sem duplicação, prazo obrigatório, empréstimos físicos e virtuais, devolução, atraso sem liberar exemplar, reserva aguardando devolução, pré-lançamento, cancelamento, retirada, integridade referencial, unicidade, concorrência no empréstimo e respostas HTTP.

Os testes de segurança e instalação usam filtros reais, incluindo CSRF, login, acesso ao PDF, permissões e instalação concorrente. Consulte o README da raiz para iniciar e configurar a biblioteca.
