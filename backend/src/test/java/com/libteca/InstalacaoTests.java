package com.libteca;

import com.libteca.config.InstalacaoConfig;
import com.libteca.dto.biblioteca.*;
import com.libteca.enums.Role;
import com.libteca.repository.*;
import com.libteca.service.BibliotecaService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.file.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("sqlite-test")
class InstalacaoTests {
    static Path pasta;
    @DynamicPropertySource static void propriedades(DynamicPropertyRegistry r) throws Exception {
        pasta=Files.createTempDirectory("libteca-instalacao-");
        r.add("spring.datasource.url",()->"jdbc:sqlite:"+pasta.resolve("teste.db"));
        r.add("libteca.instalacao.diretorio",()->pasta.toString());
    }
    @Autowired WebApplicationContext contexto;
    @Autowired InstalacaoConfig instalacao;
    @Autowired BibliotecaService biblioteca;
    @Autowired BibliotecaRepository configuracao;
    @Autowired UsuarioRepository usuarios;
    @Autowired FuncionarioRepository funcionarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    MockMvc mvc;
    String codigo;
    @BeforeEach void preparar() throws Exception {
        mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        jdbc.update("DELETE FROM funcionario");jdbc.update("DELETE FROM usuario");
        configuracao.save(new com.libteca.entity.Biblioteca());
        instalacao.gerarCodigo();codigo=Files.readString(pasta.resolve("instalacao.codigo"));
    }
    InstalacaoRequest pedido(String codigo) {return new InstalacaoRequest(new BibliotecaRequest("Biblioteca Horizonte",null),codigo,"Administrador","admin@teste.local","SenhaTeste123!");}
    String json(String codigo) {return new tools.jackson.databind.ObjectMapper().writeValueAsString(pedido(codigo));}

    @Test void instalaUmaVezSemCredenciaisPadrao() throws Exception {
        mvc.perform(post("/api/instalacao").with(csrf()).contentType("application/json").content(json(codigo))).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Biblioteca Horizonte"));
        var admin=usuarios.findByEmail("admin@teste.local").orElseThrow();
        assertEquals(Role.ADMIN,admin.getRole());assertTrue(encoder.matches("SenhaTeste123!",admin.getSenha()));
        assertTrue(funcionarios.findByUsuarioId(admin.getId()).orElseThrow().getAtivo());
        assertFalse(Files.exists(pasta.resolve("instalacao.codigo")));
        mvc.perform(post("/api/instalacao").with(csrf()).contentType("application/json").content(json(codigo))).andExpect(status().isConflict());
        assertEquals(1,usuarios.count());
        mvc.perform(post("/api/auth/entrar").with(csrf()).param("email",admin.getEmail()).param("senha","SenhaTeste123!")).andExpect(status().isNoContent());
    }
    @Test void bloqueiaCodigoInvalidoECsrfAusenteSemCriarConta() throws Exception {
        mvc.perform(post("/api/instalacao").contentType("application/json").content(json(codigo))).andExpect(status().isForbidden());
        mvc.perform(post("/api/instalacao").with(csrf()).contentType("application/json").content(json("incorreto"))).andExpect(status().isForbidden());
        assertEquals(0,usuarios.count());assertFalse(biblioteca.informacoes().configurada());
        mvc.perform(post("/api/auth/cadastro").with(csrf()).contentType("application/json").content("{\"telefone\":\"11900000001\",\"nome\":\"Leitor\",\"email\":\"leitor@teste.local\",\"senha\":\"SenhaTeste123!\"}")).andExpect(status().isConflict());
    }
    @Test void duasInstalacoesConcorrentesCriamUmAdministrador() throws Exception {
        var executor=Executors.newFixedThreadPool(2);
        var inicio=new CountDownLatch(1);
        Callable<Boolean> acao=()->{inicio.await();try{biblioteca.instalar(pedido(codigo));return true;}catch(org.springframework.web.server.ResponseStatusException e){assertEquals(409,e.getStatusCode().value());return false;}};
        try {var a=executor.submit(acao);var b=executor.submit(acao);inicio.countDown();assertNotEquals(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));assertEquals(1,usuarios.count());}
        finally {executor.shutdownNow();}
    }
    @Test void somenteAdministradorAlteraIdentidadeELeitoresRecebemNome() throws Exception {
        biblioteca.instalar(pedido(codigo));
        String dados="{\"nome\":\"Biblioteca da Escola\",\"site\":\"https://escola.example\"}";
        mvc.perform(put("/api/biblioteca").with(csrf()).contentType("application/json").content(dados)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/biblioteca").with(user("visitante@teste.local")).with(csrf()).contentType("application/json").content(dados)).andExpect(status().isForbidden());
        mvc.perform(put("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(dados)).andExpect(status().isOk());
        mvc.perform(get("/api/biblioteca")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Biblioteca da Escola")).andExpect(jsonPath("$.configurada").value(true));
        mvc.perform(put("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content("{\"nome\":\"Teste\",\"site\":\"javascript:alert(1)\"}")).andExpect(status().isBadRequest());
    }
    @Test void administradorExistenteNaoPodeSerSubstituidoPelaInstalacao() throws Exception {
        biblioteca.instalar(pedido(codigo));
        var dados=configuracao.findById(1L).orElseThrow();dados.setConfigurada(0);configuracao.save(dados);
        instalacao.gerarCodigo();codigo=Files.readString(pasta.resolve("instalacao.codigo"));
        mvc.perform(post("/api/instalacao").with(csrf()).contentType("application/json").content(json(codigo))).andExpect(status().isConflict());
        mvc.perform(put("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content("{\"nome\":\"Acervo existente\",\"site\":null}")).andExpect(status().isOk());
        assertEquals(1,usuarios.count());
    }
}
