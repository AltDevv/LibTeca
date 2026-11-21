# Frontend LibTeca

HTML, CSS e módulos JavaScript nativos, sem Flutter e sem framework. Execute pelo backend conforme o README da raiz; abrir index.html diretamente não inicia a API.

| Pasta | Responsabilidade |
| --- | --- |
| config | Endereço relativo `/api`, funciona no mesmo domínio e em túneis |
| controller | Navegação, eventos, formulários e carregamento das telas |
| dto | Conversão dos formulários em payloads da API |
| entity | Estado da sessão e exemplos visuais |
| enums | Rótulos de situações e formatos |
| filter | Parâmetros de busca do catálogo |
| handler | Mensagens de erro e avisos |
| mapper | Formatação de datas, capas e escape de texto |
| repository | HTTP, CSRF, sessão e paginação |
| service | Operações da biblioteca, autenticação e configuração |
| specification | Permissões de navegação (a API também valida o acesso) |
| telas | HTML das telas e dos formulários |
| css / assets | Aparência responsiva e marca |

O nome retornado por GET /api/biblioteca preenche `document.title`, o menu e o catálogo. O administrador pode alterá-lo em Minha biblioteca. O nome é tratado como texto, sem executar HTML inserido pelo usuário.

A primeira execução sem banco definido abre a escolha entre SQLite e MySQL. POST /api/instalacao/banco valida a conexão e salva a configuração local; após o reinício automático, POST /api/instalacao configura a biblioteca e o administrador. Ambas as etapas exigem um código local temporário. Configurações posteriores usam PUT /api/biblioteca, restrito ao administrador. Senhas e códigos não são guardados no localStorage.

Há três cartões visuais Livro exemplo enquanto o acervo nunca recebeu um livro. Sua remoção manual fica apenas neste navegador. O primeiro livro real desativa os exemplos no servidor para todos os visitantes.

Testes de apresentação, exemplos e formulários: `node --test frontEnd/tests/*.test.mjs`, a partir da raiz.

Autores, categorias e editoras ficam nas abas de Cadastros. Os formulários de livros usam caixas de seleção para múltiplos autores e categorias; o cadastro de exemplares mostra somente os campos do formato escolhido. O menu se recolhe no celular e as tabelas extensas permitem rolagem horizontal dentro da lista.

Ao selecionar uma capa JPG/PNG, a prévia permite ajustar zoom e posição por controles ou arraste. O enquadramento é enviado como uma cópia JPEG de 600 × 800 pixels ao salvar o livro; o arquivo original é preservado. Endereços de imagens continuam disponíveis como alternativa ao envio.

Administradores encontram Excluir livro nos cartões e nos detalhes, com confirmação. O backend impede a exclusão de livros com exemplares vinculados e informa o motivo.

Administradores e funcionários encontram Editar livro nos cartões e nos detalhes, e Editar na lista de exemplares. Os formulários carregam os dados atuais e salvam por PUT, preservando a capa se ela não for substituída. O backend protege os exemplares em circulação e impede alterações de livro/formato que invalidariam seu histórico.

Cada livro tem um único exemplar virtual compartilhado por vários leitores. A gestão de exemplares tem abas Físicos e Virtuais. Nos virtuais, V001, V002 etc. são códigos visuais ordenados pelo cadastro; as ações continuam usando o ID real. A tabela mostra leituras ativas e total de empréstimos. Sem PDF, o virtual fica indisponível.

A paginação acompanha as colunas do catálogo: usa 12 livros por página nas grades de duas, três ou quatro colunas e 15 nas de cinco colunas. Ao redimensionar a tela, recalcula a página mantendo a posição aproximada da leitura. Somente a última página pode ter uma linha incompleta.

O catálogo oferece Emprestar para exemplares físicos disponíveis e Reservar somente quando o formato estiver em falta. O leitor vê "Aguardando entrega pelo funcionário" e pode cancelar o pedido. Em Gestão → Empréstimos, funcionário ou administrador usa "Confirmar entrega": o sistema registra o responsável autenticado e inicia o prazo escolhido pelo leitor: 1 semana, 1 mês ou 2 meses. Reservas físicas prontas são entregues pela gestão em Reservas. O leitor não pode confirmar a entrega nem a devolução física. A leitura digital continua automática e incorporada à tela.

A pasta `frontEnd` é independente do módulo Maven `backend`. A execução `copiar-front-end` copia somente os arquivos do site para `target/classes/static`, tanto em `spring-boot:run` quanto no pacote JAR. Depois de alterar o POM, recarregue o projeto Maven no IntelliJ para atualizar a árvore.

Ao executar a classe Java diretamente pela IDE sem os recursos copiados, o perfil `web` também procura `frontEnd` na raiz do projeto ou ao lado de `backend`. No JAR, os recursos empacotados têm prioridade. Não é necessário marcar a pasta como Resources Root.

No cadastro e edição de livros, Permitir reservas habilita o limite de reservas em andamento (padrão 3). Desmarcar salva limite 0. O catálogo só oferece Reservar quando faltar exemplar do formato solicitado e houver vaga na fila do livro. A validação também é feita pelo backend.

Minha biblioteca oferece Transferir para um funcionário e Apagar sistema somente ao administrador. As confirmações exigem senha atual; a exclusão também exige o nome da biblioteca. As listas de usuários e equipe ocultam exclusão/desativação de administradores. Transferir mantém a conta anterior como funcionário; apagar retorna à configuração inicial. Nenhuma operação ocorre apenas ao abrir a tela.

## Gestão da equipe

Equipe aparece somente para o administrador. Ele seleciona uma conta de leitor já cadastrada e informa a admissão. O cadastro promove essa conta a funcionário imediatamente. Funcionários não cadastram, não solicitam inclusão, não desativam e não excluem outros funcionários. A lista de Usuários também oculta a exclusão de contas da equipe para esse perfil, e o backend aplica as mesmas restrições.

Ao solicitar empréstimo físico ou digital, o modal exige selecionar 1 semana, 1 mês ou 2 meses. `PrazoTela.js` monta as três opções, e o controlador envia o enum correspondente em `prazo`. O backend calcula a data; o formulário não envia uma data arbitrária. Não há prazo de 14 dias selecionado automaticamente para novos pedidos.

Testes de navegador: veja [TESTES_PLAYWRIGHT.md](TESTES_PLAYWRIGHT.md) para executar os 11 cenários com Playwright Java, acompanhar o Chromium e abrir os traces. Instalação, bancos e configuração da biblioteca ficam no [README principal](../README.md).
