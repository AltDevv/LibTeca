# Backend do LibTeca

Adaptação ao arquivo banco_LibTeca.mwb conferido em 12/11/2026. Foram mantidos Java, Spring Boot, Lombok, DTOs em records, mappers manuais, injeção por construtor e a divisão entre controller, dto, entity, enums, filter, handler, mapper, repository, service e specification.

Este guia descreve a implementação e a API. O passo a passo de instalação, configuração de SQLite/MySQL, execução pela IDE e primeiro uso está no [README principal](../README.md).

## Modelo

- As 12 tabelas do modelo são representadas por 10 entidades. Livro usa conjuntos de autores e categorias, com as tabelas livro_autor e livro_categoria e suas chaves primárias compostas.
- Os nomes das tabelas e colunas são explícitos nas anotações. Os IDs BIGINT usam Long.
- Funcionário complementa um usuário. A mesma conta só pode ter um cadastro de funcionário. Cadastrar um funcionário promove USER para FUNCIONARIO; ADMIN é preservado.
- ADMIN é o nome correto do perfil de administrador. ADMIM continua aceito para não invalidar registros antigos.
- O funcionário do livro identifica o responsável pelo cadastro. Em reserva e empréstimo, funcionarioId pode ser nulo.
- capa é a referência da imagem do livro. caminhoArquivo é a referência do arquivo do exemplar virtual. O upload retorna essas referências; capas são servidas publicamente e PDFs somente mediante empréstimo vigente.
- Telefone mantém o UNIQUE global definido no modelo: um número não pode pertencer a dois cadastros.

## Empréstimos e reservas

O empréstimo aponta para um exemplar. Exemplares físicos podem aguardar a confirmação de recebimento; a leitura digital começa imediatamente.

Pelo catálogo, o leitor escolhe 1 semana, 1 mês ou 2 meses e solicita o físico disponível: empréstimo PENDENTE e exemplar RESERVADO. Somente funcionário ou administrador ativo confirma em PATCH /api/emprestimos/{id}/entregar. O responsável vem da sessão autenticada, é gravado em idfuncionario e o empréstimo passa a ATIVO; o exemplar fica EMPRESTADO, com o prazo escolhido contado a partir da entrega. Enquanto pendente, as datas são uma previsão, não exibida como prazo nem processada como atraso. Confirmar novamente não prorroga o prazo. O endpoint antigo de recebimento pelo leitor rejeita a ação.

Antes do recebimento, o dono pode cancelar em `PATCH /api/minha-conta/emprestimos/{id}/cancelar`; a equipe pode usar `PATCH /api/emprestimos/{id}/cancelar`. O status CANCELADO preserva o histórico, deixa a data de devolução nula e libera o exemplar ou atende a reserva seguinte. PENDENTE e CANCELADO cabem no VARCHAR(9) existente; não exigem alteração nas tabelas. Reservas físicas prontas exigem entrega pela gestão em `/api/reservas/{id}/retirar`; o leitor só pode retirar reservas digitais sozinho. A devolução física continua sendo registrada pela equipe. Empréstimos feitos pela equipe no balcão começam ativos.

O enum `PrazoEmprestimo` aceita somente `UMA_SEMANA`, `UM_MES` e `DOIS_MESES`. `plusWeeks(1)` conta sete dias; `plusMonths(1/2)` respeita o calendário, inclusive no fim do mês. As rotas públicas de empréstimo exigem `prazo`; ausência ou valor inválido é rejeitado. O DTO interno mantém a data calculada para os serviços de circulação.

Depois da escolha e da confirmação de entrega (ou do início imediato no caso digital):

1. O backend calcula a data final usando o prazo escolhido.
2. Grava a opção em prazo_escolhido e a data em data_expiracao; data_devolucao permanece NULL.
3. Quando o prazo passa, o status pode virar EXPIRADO. O físico continua emprestado, e o acesso ao PDF vence.
4. A operação de devolver registra a data efetiva, marca DEVOLVIDO e libera o exemplar ou o destina à reserva pendente.

Não existem mais permiteDownload, possuiExpiracao, diasExpiracao, contadores de estoque em livro. A disponibilidade é calculada a partir dos exemplares. Uma devolução virtual também precisa ser registrada; o vencimento sozinho não devolve o exemplar.

O cadastro digital cria um único exemplar VIRTUAL por livro. Vários empréstimos apontam para esse mesmo ID, cada um com seu leitor e prazo. O virtual permanece disponível enquanto houver PDF, mesmo durante empréstimos. O mesmo leitor não pode abrir duas leituras vigentes do mesmo virtual. Leituras vencidas não consomem disponibilidade; a devolução encerra somente o empréstimo daquele leitor.

É possível cadastrar o digital antes de receber o PDF: sem `caminhoArquivo`, ele fica INDISPONIVEL e não aceita empréstimo. Ao editar esse exemplar e informar o arquivo, ele passa a DISPONIVEL. O cadastro e a edição atuam no único registro virtual.

A reserva é de um exemplar específico e forma uma fila por data e ID. Novas reservas só são aceitas quando não há exemplar disponível do mesmo formato (ou antes do lançamento). O limite é compartilhado por todo o livro e conta ATIVA e DISPONIVEL_PARA_RETIRADA; cada leitor pode ter uma reserva em andamento por livro. Canceladas e concluídas liberam vaga. A devolução promove a primeira reserva da fila do exemplar. A retirada conclui essa reserva e cria o empréstimo.

Reservas em andamento precisam ser canceladas antes de excluídas. Empréstimos precisam estar devolvidos ou cancelados antes da exclusão. Exclusões que violam FKs são rejeitadas, preservando os registros relacionados.

## Exemplos de requisições

Use IDs que realmente existam. Datas de admissão e aquisição abaixo são exemplos. Para empréstimos, envie uma das três opções de prazo; a API calcula a data final.

POST /api/funcionarios (cadastro direto pelo administrador)

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
{"exemplarId": 1, "usuarioId": 2, "prazo": "UM_MES"}
```

PATCH /api/emprestimos/1/devolver — sem corpo.

POST /api/reservas

```json
{"exemplarId": 1, "usuarioId": 2}
```

PATCH /api/reservas/1/retirar

```json
{"prazo": "DOIS_MESES"}
```

PATCH /api/reservas/1/cancelar — sem corpo.

POST /api/minha-conta/emprestimos/1 — o leitor solicita o exemplar físico ou virtual de ID 1; sua identidade vem da sessão:

```json
{"prazo": "UMA_SEMANA"}
```

Funcionários, exemplares e telefones possuem GET na coleção e no ID, POST e DELETE na coleção e no ID. A equipe pode consultar funcionários, mas somente o administrador pode cadastrá-los diretamente, alterá-los ou excluí-los. Funcionário também possui PUT /api/funcionarios/{id}, com o mesmo corpo do cadastro, para alterar admissão e ativo; não permite trocar o usuário.

Consultas adicionais:

- GET /api/exemplares/livro/{livroId}
- GET /api/telefones/usuario/{usuarioId}
- GET /api/livros/buscar?autor=Machado&categoria=Romance
- GET /api/livros/buscar?tipoLivro=FISICO&quantidadeMinima=1

O filtro de quantidade conta exemplares DISPONIVEL, e considera tipoLivro quando informado. As respostas do livro trazem listas autores e categorias. As respostas de empréstimo e reserva trazem exemplar, usuario, funcionarioId e status.

## Persistência

MySQL usa `ddl-auto=validate` e o esquema de `banco/schema-mysql.sql`. O perfil não recria nem repovoa o acervo. As atualizações automáticas são específicas: colunas de limite/prazo, consolidação dos virtuais e migração das configurações locais descritas neste guia. Elas não substituem migrações de outros modelos antigos.

No SQLite, `db/schema-sqlite.sql` cria tabelas ausentes com FKs, UNIQUEs e chaves compostas. O SQLite usa uma conexão por instância para serializar as escritas locais. Os dois perfis são alternativas de armazenamento, não uma sincronização entre bancos. Configuração da conexão e diretórios está no [manual de instalação](../README.md).

O SQL explícito de SQLite evita depender da geração automática de identidades e FKs pelo Hibernate. As datas usam texto yyyy-MM-dd, configurado pelas propriedades date_class e date_string_format do [driver SQLite JDBC](https://github.com/xerial/sqlite-jdbc/blob/master/src/main/java/org/sqlite/SQLiteConfig.java).

## Autenticação, configuração e arquivos

O login consulta o usuário do banco e confere BCrypt. A sessão usa cookie HttpOnly, SameSite=Lax e CSRF nas alterações. Não há senha padrão nem JWT. Funcionários inativos não acessam a gestão. O backend obtém o funcionário responsável da sessão, sem confiar no ID enviado pelo cliente.

A configuração inicial cria ADMIN + Funcionario em uma transação, usando o código temporário local. `Biblioteca` é um objeto de configuração, sem mapeamento JPA. `BibliotecaRepository` persiste `nome`, `site`, `configurada` e `acervo_iniciado` em `data/biblioteca.properties`, no diretório definido por `libteca.instalacao.diretorio`. Uma trava serializa as alterações nesta instância. A gravação substitui o arquivo por uma cópia temporária; em rollback da transação, restaura a versão anterior. Não é uma transação distribuída entre arquivo e banco: o backup da instalação precisa incluir ambos.

`ConfiguracaoLocalService` roda na inicialização. Se a tabela antiga existir, importa seus valores quando ainda não há arquivo local, salva uma cópia CSV e remove a tabela. Também guarda em CSV e remove `solicitacao_funcionario`, cujo fluxo foi descontinuado. Se `configuracao_biblioteca` já foi apagada, reconhece o administrador e o acervo existentes; nome/site ausentes voltam ao padrão e podem ser ajustados pela conta administradora. Essas tabelas não são recriadas. Falha ao salvar a configuração ou seu backup impede a remoção correspondente.

| Configuração | Função |
| --- | --- |
| ArquivoConfig | Diretório de armazenamento e limite de arquivos |
| CorsConfig | Origens explicitamente permitidas; mesma origem funciona sem liberação extra |
| JSONConfig | Serialização e rejeição de campos JSON desconhecidos |
| OpenApiConfig | Documentação da API em /swagger-ui.html, protegida para a equipe |
| SecurityConfig | Login, sessão, CSRF, BCrypt e autorização das rotas |
| InstalacaoConfig / InstalacaoInicialConfig | Geração, validade e invalidação do código de instalação |

POST /api/arquivos recebe multipart `arquivo` e `tipo` (capa ou livro), para a equipe. PNG/JPG/PDF são validados por formato e limite de tamanho. GET /capas/{nome} fornece capas; GET /api/arquivos/leitura/{emprestimoId} verifica dono, tipo virtual, prazo e devolução antes de fornecer o PDF. O diretório de PDFs não é exposto como recurso estático.

A leitura digital exige devolução e usa a escolha do leitor: 1 semana, 1 mês ou 2 meses. Funcionários escolhem entre as mesmas opções nas operações de circulação. Empréstimos antigos mantêm suas datas; somente pedidos físicos antigos sem `prazo_escolhido` conservam o cálculo legado de 14 dias na entrega. `PrazoSchemaConfig` adiciona a coluna quando ausente, sem alterar datas existentes. Exibir PDF não impede sua cópia no navegador.

## Testes

Iniciando pela pasta `backend`, os logs da aplicação ficam em `logs/libteca.log`. Para escolher outro diretório, defina `LIBTECA_LOG_DIR`. Os registros não são versionados; `.gitkeep` mantém a pasta vazia no Git. Saídas de comandos Maven podem ser direcionadas para essa mesma pasta, por exemplo: `./mvnw.cmd test *> logs/testes.log` no PowerShell.

```powershell
./mvnw.cmd test
```

Executa os fluxos em H2 e em um arquivo SQLite temporário. Para incluir MySQL, informe LIBTECA_MYSQL_TEST_URL, LIBTECA_MYSQL_TEST_USER e LIBTECA_MYSQL_TEST_PASSWORD apontando exclusivamente para um banco vazio de testes. O inicializador cria as tabelas de schema-modelo.sql; esse SQL foi extraído do .mwb, preservando colunas, PKs, UNIQUEs e FKs. Índices auxiliares são visíveis na estrutura de teste.

Os testes cobrem criação e relações, filtros sem duplicação, prazo obrigatório, empréstimos físicos e virtuais, devolução, atraso sem liberar exemplar, reserva aguardando devolução, pré-lançamento, cancelamento, retirada, integridade referencial, unicidade, concorrência no empréstimo e respostas HTTP.

Os testes de segurança e instalação usam filtros reais, incluindo CSRF, login, acesso ao PDF, permissões e instalação concorrente. Consulte o README da raiz para iniciar e configurar a biblioteca.

A coluna livro.limite_reservas guarda o máximo em andamento: 0 desativa novas reservas. O padrão é 3. ReservaSchemaConfig adiciona essa coluna nas instalações antigas, antes da validação JPA, em MySQL e SQLite, sem sobrescrever configurações ao reiniciar. Reduzir o limite ou desativar não apaga reservas existentes. A API recebe limiteReservas e retorna também reservasEmAndamento.

Acesso via ngrok: o perfil web usa server.forward-headers-strategy=native e X-Forwarded-Host para reconhecer a origem HTTPS encaminhada pelo proxy local. O Tomcat aplica seus critérios de proxies confiáveis; CORS e CSRF continuam ativos. Não é necessário liberar todas as origens nem cadastrar o domínio temporário do ngrok quando site e API usam o mesmo endereço.

VirtualUnicoService consolida os antigos acessos duplicados por livro na inicialização, sem mudar o esquema: grava backup JSON em data/migracoes, redireciona empréstimos e reservas e remove somente exemplares duplicados. PDFs diferentes interrompem a consolidação para revisão. Os IDs de empréstimos e as datas são preservados. ExemplarResponse retorna emprestimosAtivos (leituras vigentes) e totalEmprestimos (histórico sem cancelados).

## Administração da biblioteca
Administradores (ADMIN/ADMIM) não podem ser excluídos pelas rotas de usuários/equipe nem desativados. POST /api/biblioteca/transferir exige senhaAtual e funcionarioId de outro funcionário ativo: promove o destino e mantém o administrador anterior como funcionário, preservando IDs e histórico. DELETE /api/biblioteca exige senhaAtual e nomeBiblioteca exato: limpa os registros por ordem de FKs e os arquivos gerenciados em capas/livros, mantendo o programa, a conexão do banco e backups locais. Não reinicia IDs. Gera novo código local de instalação e encerra sessões. Arquivos são movidos para uma pasta temporária antes da limpeza, restaurados em rollback e removidos após commit. As duas operações são exclusivas em relação às requisições da API nesta instância, exigem CSRF e acesso de administrador, e encerram as sessões existentes após sucesso.

## Permissões da equipe

Somente o administrador pode cadastrar, alterar, desativar ou excluir funcionários. O cadastro direto promove USER a FUNCIONARIO; a exclusão do vínculo retorna essa conta a USER, desde que não existam relações que impeçam a exclusão. Funcionários também não podem excluir contas FUNCIONARIO pela rota de usuários. O perfil do usuário não é aceito no cadastro comum: novas contas começam como USER.

O fluxo de solicitação/aprovação foi removido do frontend e da API; as rotas antigas são negadas explicitamente. O administrador gerencia a equipe diretamente. Os testes de autorização verificam as rotas de funcionários e usuários; `ConfiguracaoLocalTests` cobre migração, reinício, tabela já apagada, falha de gravação e rollback.

## Telefone no cadastro

POST `/api/auth/cadastro` e POST `/api/usuarios` exigem `nome`, `email`, `senha` e `telefone`. O telefone deve conter DDD e 10 ou 11 dígitos, sem pontuação. O backend grava usuário e telefone na mesma transação; número já cadastrado retorna conflito e desfaz o cadastro. A regra vale para leitores e para cadastros feitos por funcionário ou administrador, sem alterar a estrutura das tabelas.
