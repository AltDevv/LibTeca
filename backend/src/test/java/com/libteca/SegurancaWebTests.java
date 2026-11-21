package com.libteca;

import com.libteca.entity.*;
import com.libteca.enums.*;
import com.libteca.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("sqlite-test")
class SegurancaWebTests {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registry) throws Exception {
        Path arquivo=Files.createTempFile("libteca-web-", ".db");arquivo.toFile().deleteOnExit();
        registry.add("spring.datasource.url",()->"jdbc:sqlite:"+arquivo.toAbsolutePath());
        Path pasta=Files.createTempDirectory("libteca-arquivos-teste-");
        registry.add("libteca.arquivos.diretorio",()->pasta.toString());
    }
    @Autowired WebApplicationContext contexto;
    @Autowired UsuarioRepository usuarios;
    @Autowired FuncionarioRepository funcionarios;
    @Autowired AutorRepository autores;
    @Autowired CategoriaRepository categorias;
    @Autowired EditoraRepository editoras;
    @Autowired LivroRepository livros;
    @Autowired ExemplarRepository exemplares;
    @Autowired EmprestimoRepository emprestimos;
    @Autowired PasswordEncoder encoder;
    MockMvc mvc;
    Usuario leitor,admin,outro;
    Funcionario funcionario;
    @BeforeEach void preparar(){
        mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        leitor=usuario(Role.USER);outro=usuario(Role.USER);admin=usuario(Role.ADMIN);
        funcionario=new Funcionario();funcionario.setUsuario(admin);funcionario.setAtivo(true);funcionario.setDataAdmissao(LocalDate.now());funcionarios.save(funcionario);
    }
    Usuario usuario(Role role){
        Usuario u=new Usuario();u.setNome("Teste");u.setEmail(UUID.randomUUID()+"@teste.local");u.setSenha(encoder.encode("SenhaTeste!123"));u.setRole(role);return usuarios.save(u);
    }
    @Test void catalogoPublicoEGestaoProtegida() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/api/livros")).andExpect(status().isOk());
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios").with(user(leitor.getEmail()))).andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").with(user(admin.getEmail()))).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs").with(user(admin.getEmail()))).andExpect(status().isOk()).andExpect(jsonPath("$.info.title").value("LibTeca API"));
        mvc.perform(options("/api/livros").header("Origin","https://nao-autorizado.test").header("Access-Control-Request-Method","GET")).andExpect(status().isForbidden());
    }
    @Test void loginRealComBcryptECsrf() throws Exception {
        mvc.perform(post("/api/auth/entrar").param("email",leitor.getEmail()).param("senha","SenhaTeste!123")).andExpect(status().isForbidden());
        var login=mvc.perform(post("/api/auth/entrar").with(csrf()).param("email",leitor.getEmail()).param("senha","SenhaTeste!123")).andExpect(status().isNoContent()).andReturn();
        var sessao=(org.springframework.mock.web.MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isOk()).andExpect(jsonPath("$.usuario.id").value(leitor.getId()));
        mvc.perform(post("/api/auth/entrar").with(csrf()).param("email",leitor.getEmail()).param("senha","errada")).andExpect(status().isUnauthorized());
    }
    @Test void jsonInvalidoEFuncionarioInativo() throws Exception {
        mvc.perform(post("/api/auth/cadastro").with(csrf()).contentType("application/json")
                .content("{\"nome\":\"Teste\",\"email\":\"a@teste.local\",\"senha\":\"senha123\",\"role\":\"ADMIN\"}")).andExpect(status().isBadRequest());
        funcionario.setAtivo(false);funcionarios.save(funcionario);
        mvc.perform(get("/api/usuarios").with(user(admin.getEmail()))).andExpect(status().isForbidden());
    }
    @Test void arquivoSoPodeSerLidoPeloDonoEnquantoVigente() throws Exception {
        byte[] pdf="%PDF-1.4\nArquivo de teste".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var enviado=mvc.perform(multipart("/api/arquivos").file(new MockMultipartFile("arquivo","livro.pdf","application/pdf",pdf))
                .param("tipo","livro").with(user(admin.getEmail())).with(csrf())).andExpect(status().isOk()).andReturn();
        String caminho=new tools.jackson.databind.ObjectMapper().readTree(enviado.getResponse().getContentAsString()).get("caminho").asText();
        Editora editora=new Editora();editora.setNome("Teste");editoras.save(editora);
        Livro livro=new Livro();livro.setTitulo("Teste");livro.setEditora(editora);livro.setFuncionario(funcionario);livro.setNumeroDePaginas(20);livro.setDataLancamento(LocalDate.now());livros.save(livro);
        Exemplar exemplar=new Exemplar();exemplar.setLivro(livro);exemplar.setTipo(TypeLivro.VIRTUAL);exemplar.setDataAquisicao(LocalDate.now());exemplar.setStatus(StatusExemplar.EMPRESTADO);exemplar.setCaminhoArquivo(caminho);exemplares.save(exemplar);
        Emprestimo e=new Emprestimo();e.setUsuario(leitor);e.setExemplar(exemplar);e.setDataEmprestimo(LocalDate.now());e.setDataExpiracao(LocalDate.now().plusDays(1));emprestimos.save(e);
        mvc.perform(get("/api/arquivos/leitura/"+e.getId()).with(user(leitor.getEmail()))).andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
        mvc.perform(get("/api/arquivos/leitura/"+e.getId()).with(user(outro.getEmail()))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/minha-conta/emprestimos/"+e.getId()+"/devolver").with(user(outro.getEmail())).with(csrf())).andExpect(status().isForbidden());
        e.setDataExpiracao(LocalDate.now().minusDays(1));emprestimos.save(e);
        mvc.perform(get("/api/arquivos/leitura/"+e.getId()).with(user(leitor.getEmail()))).andExpect(status().isForbidden());
        mvc.perform(multipart("/api/arquivos").file(new MockMultipartFile("arquivo","x.html","text/html","<script></script>".getBytes()))
                .param("tipo","capa").with(user(admin.getEmail())).with(csrf())).andExpect(status().isBadRequest());
    }
}
