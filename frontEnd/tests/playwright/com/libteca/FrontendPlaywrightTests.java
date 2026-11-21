package com.libteca;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;
import java.nio.file.*;
import java.util.List;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Testes de navegador reais, sem simular respostas da API. */
@Tag("playwright")
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties={"server.address=127.0.0.1","spring.jpa.show-sql=false"})
@ActiveProfiles({"sqlite-test","web"})
class FrontendPlaywrightTests {
    static final Path PASTA=temporario();
    static Path temporario(){try{return Files.createTempDirectory("libteca-playwright-");}catch(Exception e){throw new IllegalStateException(e);}}
    @DynamicPropertySource static void banco(DynamicPropertyRegistry r){
        // Não aceita URL externa: toda execução usa seu próprio SQLite temporário.
        r.add("spring.datasource.url",()->"jdbc:sqlite:"+PASTA.resolve("teste.db"));
        r.add("libteca.arquivos.diretorio",()->PASTA.resolve("arquivos").toString());
        r.add("libteca.instalacao.diretorio",()->PASTA.toString());
    }
    @Value("${local.server.port}") int porta;
    @Autowired JdbcTemplate jdbc;
    @Autowired com.libteca.repository.BibliotecaRepository configuracao;
    @Autowired PasswordEncoder encoder;
    static Playwright playwright;
    static Browser browser;
    BrowserContext contexto;
    Page pagina;
    @BeforeAll static void navegador(){
        playwright=Playwright.create();
        browser=playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(!Boolean.getBoolean("playwright.headed"))
                .setSlowMo(Double.parseDouble(System.getProperty("playwright.slowmo","0"))));
    }
    @AfterAll static void encerrar(){if(browser!=null)browser.close();if(playwright!=null)playwright.close();}
    @BeforeEach void preparar(){
        for(String tabela:List.of("reserva","emprestimo","telefone","livro_autor","livro_categoria","exemplar","livro","funcionario","usuario","autor","categoria","editora"))jdbc.update("delete from "+tabela);
        var dados=new com.libteca.entity.Biblioteca();dados.setNome("Biblioteca de Teste");dados.setConfigurada(1);dados.setAcervoIniciado(1);configuracao.save(dados);
        String senha=encoder.encode("SenhaTeste123!");
        for(int i=1;i<=3;i++)jdbc.update("insert into usuario(idusuario,nome_usuario,email,senha,perfil_acesso) values (?,?,?,?,?)",i,List.of("Admin","Atendente","Leitor").get(i-1),List.of("admin","funcionario","leitor").get(i-1)+"@teste.local",senha,List.of("ADMIN","FUNCIONARIO","USER").get(i-1));
        jdbc.update("insert into funcionario(idfuncionario,idusuario,data_admissao,ativo) values (1,1,'2020-01-01',1),(2,2,'2020-01-01',1)");
        jdbc.update("insert into editora(ideditora,nome_editora) values (1,'Editora Teste')");
        jdbc.update("insert into autor(idautor,nome_autor) values (1,'Autora Teste')");
        jdbc.update("insert into categoria(idcategoria,tipo_categoria) values (1,'Aventura')");
        for(int i=1;i<=2;i++){
            jdbc.update("insert into livro(idlivro,ideditora,idfuncionario,titulo,numero_de_paginas,data_lancamento,limite_reservas) values (?,1,1,?,120,'2020-01-01',3)",i,i==1?"Viagem de Teste":"Jardim de Teste");
            jdbc.update("insert into livro_autor(idlivro,idautor) values (?,1)",i);
            jdbc.update("insert into livro_categoria(idlivro,idcategoria) values (?,1)",i);
            jdbc.update("insert into exemplar(idexemplar,idlivro,status_exemplar,tipo_exemplar,data_aquisicao,codigo_patrimonio) values (?,?,'DISPONIVEL','FISICO','2020-01-01',?)",i,i,"TESTE-"+i);
        }
        contexto=browser.newContext(new Browser.NewContextOptions().setViewportSize(1280,800));
        contexto.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
        pagina=contexto.newPage();pagina.setDefaultTimeout(10000);
    }
    @AfterEach void evidencia(TestInfo teste)throws Exception{
        if(contexto==null)return;
        Path destino=Path.of(System.getProperty("playwright.output","target/playwright"),teste.getTestMethod().orElseThrow().getName());Files.createDirectories(destino);
        try{if(pagina!=null&&!pagina.isClosed())pagina.screenshot(new Page.ScreenshotOptions().setPath(destino.resolve("tela.png")).setFullPage(true));}
        finally{try{contexto.tracing().stop(new Tracing.StopOptions().setPath(destino.resolve("trace.zip")));}finally{contexto.close();}}
    }
    void abrir(String rota){pagina.navigate("http://127.0.0.1:"+porta+"/#/"+rota);}
    Locator botao(String nome){return pagina.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(nome).setExact(true));}
    void entrar(String conta){
        abrir("login");pagina.locator("#form-auth [name=email]").fill(conta+"@teste.local");
        pagina.locator("#form-auth [name=senha]").fill("SenhaTeste123!");pagina.locator("#form-auth button").click();
        assertThat(pagina.locator("#conteudo h1")).hasText("Catálogo");
    }
    void sair(){botao("Sair").click();assertThat(pagina.locator(".conta a[href='#/login']")).isVisible();assertThat(pagina.locator("#conteudo h1")).hasText("Catálogo");}
    void confirmar(){pagina.locator("#confirmar-acao").click();assertThat(pagina.locator("#modal")).not().isVisible();}

    @Test @DisplayName("01 — Cadastro de leitor pelo formulário")
    void cadastroDeLeitor(){
        abrir("cadastro");pagina.getByLabel("Seu nome").fill("Nova Leitora");pagina.getByLabel("Email",new Page.GetByLabelOptions().setExact(true)).fill("nova@teste.local");pagina.getByLabel("Senha",new Page.GetByLabelOptions().setExact(true)).fill("SenhaTeste123!");
        botao("Criar minha conta").click();assertThat(pagina.locator(".nome-conta")).hasText("Nova Leitora");assertThat(pagina.locator("#conteudo h1")).hasText("Catálogo");
    }
    @Test @DisplayName("02 — Login recusa senha incorreta")
    void senhaIncorreta(){
        abrir("login");pagina.locator("[name=email]").fill("leitor@teste.local");pagina.locator("[name=senha]").fill("SenhaErrada123");pagina.locator("#form-auth button").click();
        assertThat(pagina.locator("#form-auth .form-error")).containsText("Email ou senha inválidos");assertThat(pagina.locator(".nome-conta")).hasCount(0);
    }
    @Test @DisplayName("03 — Login e encerramento da sessão")
    void sairDaConta(){entrar("leitor");botao("Sair").click();assertThat(pagina.locator(".conta-link")).hasCount(0);assertThat(pagina.locator(".conta a[href='#/login']")).isVisible();}
    @Test @DisplayName("04 — Busca por título e limpeza de filtros")
    void buscarLivro(){
        abrir("catalogo");assertThat(pagina.locator(".book-card")).hasCount(2);pagina.getByLabel("Buscar por título").fill("Viagem");botao("Buscar").click();
        assertThat(pagina.locator(".book-card")).hasCount(1);assertThat(pagina.locator(".book-card h3")).hasText("Viagem de Teste");botao("Limpar filtros").click();assertThat(pagina.locator(".book-card")).hasCount(2);
    }
    @Test @DisplayName("05 — Busca sem resultados mostra estado vazio")
    void buscaSemResultado(){abrir("catalogo");pagina.getByLabel("Buscar por título").fill("InexistenteXYZ");botao("Buscar").click();assertThat(pagina.locator("#livros-grid")).containsText("Nenhum livro encontrado");assertThat(pagina.locator(".book-card")).hasCount(0);}
    @Test @DisplayName("06 — Navegação em tela de celular")
    void menuMobile(){
        pagina.setViewportSize(390,844);entrar("leitor");pagina.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Abrir navegação")).click();
        pagina.locator("#sidebar a[href='#/emprestimos']").click();assertThat(pagina.locator("#conteudo h1")).hasText("Meus Empréstimos");
        org.junit.jupiter.api.Assertions.assertEquals(false,pagina.evaluate("document.documentElement.scrollWidth > innerWidth"));
    }
    @Test @DisplayName("07 — Leitor não vê gestão e não abre equipe")
    void permissaoDoLeitor(){entrar("leitor");assertThat(pagina.locator("#sidebar a[href='#/equipe']")).hasCount(0);abrir("equipe");assertThat(pagina.locator("#conteudo h1")).hasText("Catálogo");assertThat(pagina.locator("[data-acao=aprovar-funcionario]")).hasCount(0);}
    @Test @DisplayName("08 — Administrador cadastra categoria pelo navegador")
    void cadastrarCategoria(){
        entrar("admin");abrir("cadastros/categorias");botao("Cadastrar categoria").click();pagina.locator("#form-cadastro [name=nome]").fill("Ciência de Teste");botao("Salvar cadastro").click();
        assertThat(pagina.locator("#modal")).not().isVisible();assertThat(pagina.locator("tbody")).containsText("Ciência de Teste");pagina.reload();assertThat(pagina.locator("tbody")).containsText("Ciência de Teste");
    }
    @Test @DisplayName("09 — Empréstimo físico aguarda entrega e permite cancelamento")
    void emprestimoFisico(){
        entrar("leitor");abrir("livro/1");assertThat(pagina.locator("[data-acao=reservar]")).hasCount(0);pagina.locator("[data-acao=emprestar-fisico]").click();assertThat(pagina.locator("[name=prazo] option")).hasCount(4);pagina.locator("[name=prazo]").selectOption("DOIS_MESES");confirmar();
        abrir("emprestimos");assertThat(pagina.locator(".loan-row")).containsText("Aguardando entrega pelo funcionário");assertThat(pagina.locator("[data-acao=entregar-gestao]")).hasCount(0);
        assertThat(pagina.locator(".loan-row")).containsText("2 meses");botao("Cancelar solicitação").click();confirmar();assertThat(pagina.locator(".loan-row")).containsText("Cancelado");
    }
    @Test @DisplayName("11 — Leitor escolhe os três prazos no empréstimo digital")
    void prazosDigitais() throws Exception {
        Path arquivo=PASTA.resolve("arquivos/livros/prazo.pdf");
        Files.createDirectories(arquivo.getParent());
        Files.writeString(arquivo,"%PDF-1.4\nArquivo demonstrativo do teste de prazo");
        jdbc.update("insert into exemplar(idexemplar,idlivro,status_exemplar,tipo_exemplar,data_aquisicao,caminho_arquivo) values (3,1,'DISPONIVEL','VIRTUAL','2020-01-01','livros/prazo.pdf')");
        entrar("leitor");
        String[] valores={"UMA_SEMANA","UM_MES","DOIS_MESES"};
        String[] nomes={"1 semana","1 mês","2 meses"};
        for(int i=0;i<valores.length;i++) {
            abrir("livro/1");pagina.locator("[data-acao=emprestar-digital]").click();
            assertThat(pagina.locator("[name=prazo] option")).hasText(new String[]{"Selecione o prazo…","1 semana","1 mês","2 meses"});
            pagina.locator("#confirmar-acao").click();
            assertThat(pagina.locator("#modal")).isVisible();
            org.junit.jupiter.api.Assertions.assertEquals(i,jdbc.queryForObject("select count(*) from emprestimo",Integer.class));
            pagina.getByLabel("Tempo de empréstimo").selectOption(valores[i]);
            var inicio=java.time.LocalDate.now();confirmar();abrir("emprestimos");
            Locator ativo=pagina.locator(".loan-row").filter(new Locator.FilterOptions().setHas(pagina.locator("[data-acao=devolver]")));
            assertThat(ativo).containsText("Duração: "+nomes[i]);
            var esperado=i==0?inicio.plusWeeks(1):inicio.plusMonths(i);
            org.junit.jupiter.api.Assertions.assertEquals(valores[i],jdbc.queryForObject("select prazo_escolhido from emprestimo where status_emprestimo='ATIVO'",String.class));
            org.junit.jupiter.api.Assertions.assertEquals(esperado.toString(),jdbc.queryForObject("select data_expiracao from emprestimo where status_emprestimo='ATIVO'",String.class));
            ativo.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Devolver").setExact(true)).click();confirmar();
            assertThat(pagina.locator("[data-acao=devolver]")).hasCount(0);
        }
    }
    @Test @DisplayName("10 — Somente administrador cadastra funcionários")
    void equipeSomenteAdministrador(){
        entrar("funcionario");assertThat(pagina.locator("#sidebar a[href='#/equipe']")).hasCount(0);
        abrir("equipe");assertThat(pagina.locator("#conteudo h1")).hasText("Catálogo");
        abrir("usuarios");
        assertThat(pagina.locator("tr").filter(new Locator.FilterOptions().setHasText("funcionario@teste.local")).locator("[data-acao=excluir]")).hasCount(0);
        sair();entrar("admin");abrir("equipe");botao("Cadastrar funcionário").click();
        pagina.locator("#form-cadastro [name=usuarioId]").selectOption("3");botao("Salvar cadastro").click();
        assertThat(pagina.locator("#modal")).not().isVisible();assertThat(pagina.locator("tbody")).containsText("leitor@teste.local");
    }
}
