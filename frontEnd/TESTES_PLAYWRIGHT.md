# Testes do frontend com Playwright Java

Os 11 testes estão em `frontEnd/tests/playwright/com/libteca/FrontendPlaywrightTests.java`. Usam Playwright Java 1.64.0 com JUnit como executor. O backend continua com seus testes JUnit. Os testes JavaScript em `frontEnd/tests` são testes rápidos complementares.

## Preparação

No terminal PowerShell, entre na pasta `frontEnd` do projeto. Use o mesmo JDK configurado para executar o backend e recarregue o Maven no IntelliJ depois de alterar o pom.xml.

Instale o navegador uma vez (repita se atualizar a versão do Playwright):

```powershell
.\testar.cmd exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.classpathScope=test" "-Dexec.args=install chromium"
```

## Executar os 11 testes

Sem mostrar o navegador, mais rápido:

```powershell
.\testar.cmd test
```

Com o navegador visível e ações mais lentas para acompanhar:

```powershell
.\testar.cmd "-Dplaywright.headed=true" "-Dplaywright.slowmo=250" test
```

Somente um cenário:

```powershell
.\testar.cmd "-Dtest=FrontendPlaywrightTests#buscarLivro" "-Dplaywright.headed=true" test
```

O perfil `front-playwright` seleciona somente os testes com a tag playwright. O comando habitual `mvnw.cmd test`, sem esse perfil, continua executando os testes Java do backend sem abrir navegador. Não é necessário iniciar o servidor manualmente, usar ngrok ou informar sua senha: os testes iniciam outro servidor em porta livre e criam dados fictícios em um SQLite temporário, com uploads e configuração também isolados. Nenhuma operação usa o banco ou o acervo da biblioteca em execução.

## Cenários

1. Cadastro de leitor pelo formulário, com entrada automática.
2. Login com senha incorreta e mensagem de erro.
3. Login correto e saída da conta.
4. Busca por título e limpeza dos filtros.
5. Busca sem resultados.
6. Menu em viewport de celular (390 × 844), navegação e ausência de rolagem horizontal.
7. Leitor sem acesso à gestão e impedido de abrir Equipe.
8. Cadastro de categoria pelo administrador, persistindo após recarregar.
9. Solicitação de empréstimo físico, espera pela entrega e cancelamento pelo leitor.
10. Funcionário sem acesso à Equipe e sem exclusão de funcionários; cadastro direto pelo administrador.
11. Empréstimo digital exige escolha e respeita cada um dos três prazos, verificando também a data gravada no banco.

As interações são feitas pelo navegador com a API real. Os dados de preparação são recriados antes de cada cenário, e cada teste usa um contexto de navegador independente. Os testes rodam em sequência; não configure execução paralela desta classe porque ela recria a mesma base temporária entre cenários.

## Resultados e diagnóstico

O terminal mostra quantos passaram ou falharam. O IntelliJ também permite executar a classe ou um método pelo ícone verde; para acompanhar visualmente, acrescente `-Dplaywright.headed=true -Dplaywright.slowmo=250` às VM options da configuração de execução JUnit.

- Relatórios JUnit: `frontEnd/target/surefire-reports`.
- Screenshot final e trace de cada cenário: `frontEnd/target/playwright/<nomeDoMetodo>/tela.png` e `trace.zip`, inclusive quando uma verificação falha.
- Os artefatos ficam em target e não precisam ir para o GitHub. Cada execução substitui as evidências anteriores do mesmo cenário.

Para abrir o trace de uma execução no visualizador do Playwright:

```powershell
.\testar.cmd exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.classpathScope=test" "-Dexec.args=show-trace ../frontEnd/target/playwright/buscarLivro/trace.zip"
```

Na versão Java, os comandos são Maven/JUnit; `npx playwright test --ui` pertence ao executor JavaScript e não executa esta classe. O navegador visível e o visualizador de traces são as opções para acompanhar estes testes.

## Separação do backend

Na pasta backend, execute `.\mvnw.cmd test` para os testes JUnit do backend. Na pasta frontEnd, execute `.\testar.cmd test` para os 11 testes Playwright. O script do frontend reutiliza o Maven e o backend para iniciar o servidor temporário, mas seleciona somente as fontes em frontEnd/tests/playwright, com classes e relatórios em frontEnd/target. A dependência Playwright é carregada apenas nesse perfil. Não é preciso instalar Maven separadamente.

No IntelliJ, ative o perfil Maven front-playwright para reconhecer os testes Java dentro do frontend; desative-o para trabalhar com os testes JUnit do backend. Os comandos acima fazem essa seleção automaticamente.
