package com.libteca;

import com.libteca.config.*;
import com.libteca.dto.biblioteca.*;
import com.libteca.dto.usuario.UsuarioRequest;
import com.libteca.dto.funcionario.FuncionarioRequest;
import com.libteca.enums.Role;
import com.libteca.service.*;
import com.libteca.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles(resolver=AdministracaoTests.Perfil.class)
class AdministracaoTests {
    public static class Perfil implements ActiveProfilesResolver {
        @Override public String[] resolve(Class<?> classe){return new String[]{System.getenv("LIBTECA_MYSQL_TEST_URL")==null?"sqlite-test":"mysql-test"};}
    }
    static Path pasta;
    @DynamicPropertySource static void propriedades(DynamicPropertyRegistry r)throws Exception{
        String mysql=System.getenv("LIBTECA_MYSQL_TEST_URL");
        if(mysql!=null&&!mysql.matches("jdbc:mysql://[^/]+/libteca_teste_digitais_[a-f0-9]{32}(\\?.*)?"))
            throw new IllegalStateException("Os testes de exclusão exigem um banco MySQL temporário criado pelo helper");
        pasta=Files.createTempDirectory("libteca-administracao-");
        if(System.getenv("LIBTECA_MYSQL_TEST_URL")==null)r.add("spring.datasource.url",()->"jdbc:sqlite:"+pasta.resolve("teste.db"));
        r.add("libteca.instalacao.diretorio",()->pasta.toString());
        r.add("libteca.arquivos.diretorio",()->pasta.resolve("arquivos").toString());
    }
    @Autowired WebApplicationContext contexto;
    @Autowired BibliotecaService biblioteca;
    @Autowired BibliotecaRepository configuracao;
    @Autowired UsuarioService usuarios;
    @Autowired UsuarioRepository repository;
    @Autowired FuncionarioService funcionarios;
    @Autowired InstalacaoConfig codigo;
    @Autowired JdbcTemplate jdbc;
    @Autowired AdministracaoConfig.Sessoes sessoes;
    MockMvc mvc;Long admin,funcionario,novo;String senha="SenhaTeste123!";
    @BeforeEach void preparar()throws Exception{
        mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        for(String tabela:List.of("reserva","emprestimo","telefone","livro_autor","livro_categoria","exemplar","livro","funcionario","usuario","autor","categoria","editora"))jdbc.update("delete from "+tabela);
        configuracao.save(new com.libteca.entity.Biblioteca());
        codigo.gerarCodigo();
        biblioteca.instalar(new InstalacaoRequest(new BibliotecaRequest("Biblioteca Teste",null),Files.readString(pasta.resolve("instalacao.codigo")),"Admin","admin@teste.local",senha));
        admin=repository.findByEmail("admin@teste.local").orElseThrow().getId();
        funcionario=jdbc.queryForObject("select idfuncionario from funcionario where idusuario=?",Long.class,admin);
        novo=usuarios.adicionarUsuario(new UsuarioRequest("Atendente","funcionario@teste.local",senha)).id();
        funcionarios.adicionarFuncionario(new FuncionarioRequest(novo,LocalDate.now(),true));
        for(String tipo:List.of("capas","livros")){Files.createDirectories(pasta.resolve("arquivos/"+tipo));Files.writeString(pasta.resolve("arquivos/"+tipo+"/teste.dat"),"arquivo teste");}
    }
    String transferencia(String password){return "{\"funcionarioId\":"+jdbc.queryForObject("select idfuncionario from funcionario where idusuario=?",Long.class,novo)+",\"senhaAtual\":\""+password+"\"}";}
    String apagar(String nome,String password){return "{\"nomeBiblioteca\":\""+nome+"\",\"senhaAtual\":\""+password+"\"}";}
    @Test void somenteAdministradorGerenciaFuncionariosSemSolicitacoes()throws Exception{
        Long candidato=usuarios.adicionarUsuario(new UsuarioRequest("Candidato","candidato@teste.local",senha)).id();
        String cadastro="{\"usuarioId\":"+candidato+",\"dataAdmissao\":\""+LocalDate.now()+"\",\"ativo\":true}";
        Long atendente=jdbc.queryForObject("select idfuncionario from funcionario where idusuario=?",Long.class,novo);
        mvc.perform(post("/api/funcionarios").with(user("funcionario@teste.local")).with(csrf()).contentType("application/json").content(cadastro)).andExpect(status().isForbidden());
        mvc.perform(put("/api/funcionarios/"+atendente).with(user("funcionario@teste.local")).with(csrf()).contentType("application/json").content(cadastro)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/funcionarios/"+atendente).with(user("funcionario@teste.local")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/funcionarios").with(user("funcionario@teste.local")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/usuarios/"+novo).with(user("funcionario@teste.local")).with(csrf())).andExpect(status().isForbidden());
        for(String email:List.of("admin@teste.local","funcionario@teste.local"))
            mvc.perform(post("/api/solicitacoes-funcionarios").with(user(email)).with(csrf()).contentType("application/json").content(cadastro)).andExpect(status().isForbidden());
        assertEquals(Role.USER,repository.findById(candidato).orElseThrow().getRole());
        assertEquals(2,jdbc.queryForObject("select count(*) from funcionario",Integer.class));
        mvc.perform(post("/api/funcionarios").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(cadastro)).andExpect(status().isOk());
        Long cadastrado=jdbc.queryForObject("select idfuncionario from funcionario where idusuario=?",Long.class,candidato);
        assertEquals(Role.FUNCIONARIO,repository.findById(candidato).orElseThrow().getRole());
        mvc.perform(delete("/api/funcionarios/"+cadastrado).with(user("admin@teste.local")).with(csrf())).andExpect(status().isOk());
        assertEquals(Role.USER,repository.findById(candidato).orElseThrow().getRole());
    }
    @Test void impedeExclusaoEDesativacaoDeAdministrador()throws Exception{
        mvc.perform(delete("/api/usuarios/"+admin).with(user("funcionario@teste.local")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(delete("/api/usuarios/"+admin).with(user("admin@teste.local")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(delete("/api/usuarios").with(user("admin@teste.local")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(delete("/api/funcionarios/"+funcionario).with(user("admin@teste.local")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(delete("/api/funcionarios").with(user("admin@teste.local")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(put("/api/funcionarios/"+funcionario).with(user("admin@teste.local")).with(csrf()).contentType("application/json")
                .content("{\"usuarioId\":"+admin+",\"dataAdmissao\":\""+LocalDate.now()+"\",\"ativo\":false}")).andExpect(status().isConflict());
        assertEquals(Role.ADMIN,repository.findById(admin).orElseThrow().getRole());assertEquals(2,repository.count());
    }
    @Test void transfereSomenteComSenhaDoAdministradorEParaFuncionarioAtivo()throws Exception{
        var endpoint="/api/biblioteca/transferir";
        mvc.perform(post(endpoint).with(user("funcionario@teste.local")).with(csrf()).contentType("application/json").content(transferencia(senha))).andExpect(status().isForbidden());
        mvc.perform(post(endpoint).with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(transferencia("errada"))).andExpect(status().isForbidden());
        jdbc.update("update funcionario set ativo=0 where idusuario=?",novo);
        mvc.perform(post(endpoint).with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(transferencia(senha))).andExpect(status().isConflict());
        jdbc.update("update funcionario set ativo=1 where idusuario=?",novo);
        var sessao=new MockHttpSession();sessoes.sessionCreated(new HttpSessionEvent(sessao));
        mvc.perform(post(endpoint).with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(transferencia(senha))).andExpect(status().isOk());
        assertTrue(sessao.isInvalid());assertEquals(Role.ADMIN,repository.findById(novo).orElseThrow().getRole());
        assertEquals(Role.FUNCIONARIO,repository.findById(admin).orElseThrow().getRole());assertEquals(2,repository.count());
        mvc.perform(post(endpoint).with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(transferencia(senha))).andExpect(status().isForbidden());
        assertTrue(Files.exists(pasta.resolve("arquivos/livros/teste.dat")));
    }
    @Test void exclusaoExigeConfirmacaoLimpaDadosEArquivosEPermiteNovaInstalacao()throws Exception{
        mvc.perform(delete("/api/biblioteca").with(user("funcionario@teste.local")).with(csrf()).contentType("application/json").content(apagar("Biblioteca Teste",senha))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(apagar("Biblioteca Teste","errada"))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(apagar("outro nome",senha))).andExpect(status().isConflict());
        assertEquals(2,repository.count());assertTrue(Files.exists(pasta.resolve("arquivos/livros/teste.dat")));
        // Fixture com FKs reais: livro, exemplar, empréstimo, reserva e associações.
        jdbc.update("insert into editora(nome_editora) values ('Editora')");
        long editora=jdbc.queryForObject("select max(ideditora) from editora",Long.class);
        jdbc.update("insert into livro(ideditora,idfuncionario,titulo,numero_de_paginas,data_lancamento) values (?,?,'Livro',100,?)",editora,funcionario,LocalDate.now().toString());
        long livro=jdbc.queryForObject("select max(idlivro) from livro",Long.class);
        jdbc.update("insert into exemplar(idlivro,tipo_exemplar,status_exemplar,data_aquisicao,codigo_patrimonio) values (?,'FISICO','EMPRESTADO',?,'P1')",livro,LocalDate.now().toString());
        long exemplar=jdbc.queryForObject("select max(idexemplar) from exemplar",Long.class);
        jdbc.update("insert into emprestimo(idusuario,idexemplar,idfuncionario,status_emprestimo,data_emprestimo,data_expiracao) values (?,?,?,'ATIVO',?,?)",novo,exemplar,funcionario,LocalDate.now().toString(),LocalDate.now().plusDays(14).toString());
        jdbc.update("insert into reserva(idusuario,idexemplar,status_reserva,data_reserva) values (?,?,'ATIVA',?)",admin,exemplar,LocalDate.now().toString());
        var sessao=new MockHttpSession();sessoes.sessionCreated(new HttpSessionEvent(sessao));
        mvc.perform(delete("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(apagar("Biblioteca Teste",senha))).andExpect(status().isOk());
        assertTrue(sessao.isInvalid());assertEquals(0,repository.count());assertFalse(biblioteca.informacoes().configurada());
        assertEquals(0,jdbc.queryForObject("select count(*) from emprestimo",Integer.class));assertEquals(0,jdbc.queryForObject("select count(*) from livro",Integer.class));
        assertFalse(Files.exists(pasta.resolve("arquivos/capas/teste.dat")));assertFalse(Files.exists(pasta.resolve("arquivos/livros/teste.dat")));
        String novoCodigo=Files.readString(pasta.resolve("instalacao.codigo"));
        biblioteca.instalar(new InstalacaoRequest(new BibliotecaRequest("Nova Biblioteca",null),novoCodigo,"Novo admin","novo@teste.local",senha));
        assertTrue(biblioteca.informacoes().configurada());assertEquals(1,repository.count());
    }
    @Test void falhaNoBancoRestauraArquivosEDados()throws Exception{
        jdbc.execute("create table bloqueio_exclusao_teste(id BIGINT PRIMARY KEY,idusuario BIGINT,foreign key(idusuario) references usuario(idusuario))");
        try{
            jdbc.update("insert into bloqueio_exclusao_teste(id,idusuario) values (1,?)",admin);
            mvc.perform(delete("/api/biblioteca").with(user("admin@teste.local")).with(csrf()).contentType("application/json").content(apagar("Biblioteca Teste",senha))).andExpect(status().isConflict());
            assertEquals(2,repository.count());assertTrue(biblioteca.informacoes().configurada());
            assertEquals(Role.ADMIN,repository.findById(admin).orElseThrow().getRole());
            assertTrue(Files.exists(pasta.resolve("arquivos/capas/teste.dat")));
            assertTrue(Files.exists(pasta.resolve("arquivos/livros/teste.dat")));
            assertFalse(Files.exists(pasta.resolve("instalacao.codigo")));
        } finally {jdbc.execute("drop table bloqueio_exclusao_teste");}
    }
}
