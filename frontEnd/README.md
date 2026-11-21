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

A pasta `frontEnd` é independente do módulo Maven `backend`. A execução `copiar-front-end` copia somente os arquivos do site para `target/classes/static`, tanto em `spring-boot:run` quanto no pacote JAR. Depois de alterar o POM, recarregue o projeto Maven no IntelliJ para atualizar a árvore.

Ao executar a classe Java diretamente pela IDE sem os recursos copiados, o perfil `web` também procura `frontEnd` na raiz do projeto ou ao lado de `backend`. No JAR, os recursos empacotados têm prioridade. Não é necessário marcar a pasta como Resources Root.
