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
        registry.add("libteca.instalacao.diretorio",()->pasta.toString());
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
    @Autowired TelefoneRepository telefones;
    @Autowired BibliotecaRepository configuracao;
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

    @Test void cadastroExigeTelefoneParaLeitorEquipeEAdministradorESalvaSemOrfaos() throws Exception {
        var b=configuracao.findById(1L).orElseThrow();b.setConfigurada(1);configuracao.save(b);
        var atendente=usuario(Role.FUNCIONARIO);
        var f=new Funcionario();f.setUsuario(atendente);f.setAtivo(true);f.setDataAdmissao(LocalDate.now());funcionarios.save(f);
        for(int i=0;i<3;i++) {
            String rota=i==0?"/api/auth/cadastro":"/api/usuarios";
            String email=UUID.randomUUID()+"@teste.local",numero="1190000010"+i;
            String base="{\"nome\":\"Cadastro telefone\",\"email\":\""+email+"\",\"senha\":\"SenhaTeste123!\"";
            var identidade=user(i==1?atendente.getEmail():admin.getEmail());
            mvc.perform(post(rota).with(identidade).with(csrf()).contentType("application/json").content(base+"}")).andExpect(status().isBadRequest());
            mvc.perform(post(rota).with(identidade).with(csrf()).contentType("application/json").content(base+",\"telefone\":\"123\"}")).andExpect(status().isBadRequest());
            mvc.perform(post(rota).with(identidade).with(csrf()).contentType("application/json").content(base+",\"telefone\":\""+numero+"\"}")).andExpect(status().isOk());
            var salvo=usuarios.findByEmail(email).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals(numero,telefones.findByUsuarioId(salvo.getId()).get(0).getNumero());
            String repetido=UUID.randomUUID()+"@teste.local";
            mvc.perform(post(rota).with(identidade).with(csrf()).contentType("application/json").content(base.replace(email,repetido)+",\"telefone\":\""+numero+"\"}")).andExpect(status().isConflict());
            org.junit.jupiter.api.Assertions.assertFalse(usuarios.existsByEmail(repetido));
        }
    }

    Exemplar fisico(){
        Editora editora=new Editora();editora.setNome("Teste físico");editoras.save(editora);
        Livro livro=new Livro();livro.setTitulo("Livro físico");livro.setEditora(editora);livro.setFuncionario(funcionario);livro.setNumeroDePaginas(100);livro.setDataLancamento(LocalDate.now());livros.save(livro);
        Exemplar exemplar=new Exemplar();exemplar.setLivro(livro);exemplar.setTipo(TypeLivro.FISICO);exemplar.setDataAquisicao(LocalDate.now());exemplar.setStatus(StatusExemplar.DISPONIVEL);exemplar.setCodigoPatrimonio(UUID.randomUUID().toString());return exemplares.save(exemplar);
    }

    @Test void administradorEntregaELeitorSoPodeCancelarPedidoFisico() throws Exception {
        var exemplar=fisico();
        String pedir="/api/minha-conta/emprestimos/"+exemplar.getId();
        mvc.perform(post(pedir).with(user(leitor.getEmail()))).andExpect(status().isForbidden());
        var criado=mvc.perform(post(pedir).with(user(leitor.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE")).andReturn();
        long id=new tools.jackson.databind.ObjectMapper().readTree(criado.getResponse().getContentAsString()).get("id").asLong();
        String caminho="/api/minha-conta/emprestimos/"+id;
        mvc.perform(post(pedir).with(user(outro.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}")).andExpect(status().isConflict());
        mvc.perform(patch(caminho+"/receber").with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(patch(caminho+"/receber").with(user(outro.getEmail())).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(patch(caminho+"/cancelar").with(user(outro.getEmail())).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(patch(caminho+"/receber").with(user(leitor.getEmail())).with(csrf())).andExpect(status().isForbidden());
        String entrega="/api/emprestimos/"+id+"/entregar";
        mvc.perform(patch(entrega).with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(patch(entrega).with(user(leitor.getEmail())).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(patch(entrega).with(user(admin.getEmail()))).andExpect(status().isForbidden());
        mvc.perform(patch(entrega).with(user(admin.getEmail())).with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionarioId").value(funcionario.getId()))
                .andExpect(jsonPath("$.status").value("ATIVO")).andExpect(jsonPath("$.dataExpiracao").value(LocalDate.now().plusWeeks(1).toString()));
        mvc.perform(patch(entrega).with(user(admin.getEmail())).with(csrf())).andExpect(status().isConflict());
        mvc.perform(patch(caminho+"/cancelar").with(user(leitor.getEmail())).with(csrf())).andExpect(status().isConflict());
        mvc.perform(patch(caminho+"/devolver").with(user(leitor.getEmail())).with(csrf())).andExpect(status().isConflict());
        mvc.perform(patch("/api/emprestimos/"+id+"/devolver").with(user(admin.getEmail())).with(csrf())).andExpect(status().isOk());
        var novo=mvc.perform(post(pedir).with(user(leitor.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}")).andExpect(status().isOk()).andReturn();
        long outroId=new tools.jackson.databind.ObjectMapper().readTree(novo.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/minha-conta/emprestimos/"+outroId+"/cancelar").with(user(leitor.getEmail())).with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test void funcionarioEntregaReservaFisicaELeitorNaoPodeConfirmar() throws Exception {
        admin.setRole(Role.FUNCIONARIO);usuarios.save(admin);
        var exemplar=fisico();
        var ocupado=mvc.perform(post("/api/minha-conta/emprestimos/"+exemplar.getId()).with(user(outro.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}")).andExpect(status().isOk()).andReturn();
        long pedidoId=new tools.jackson.databind.ObjectMapper().readTree(ocupado.getResponse().getContentAsString()).get("id").asLong();
        var criada=mvc.perform(post("/api/minha-conta/reservas/"+exemplar.getId()).with(user(leitor.getEmail())).with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVA")).andReturn();
        long id=new tools.jackson.databind.ObjectMapper().readTree(criada.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/minha-conta/emprestimos/"+pedidoId+"/cancelar").with(user(outro.getEmail())).with(csrf())).andExpect(status().isOk());
        mvc.perform(patch("/api/minha-conta/reservas/"+id+"/retirar").with(user(outro.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/minha-conta/reservas/"+id+"/retirar").with(user(leitor.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/reservas/"+id+"/retirar").with(user(admin.getEmail())).with(csrf())
                .contentType("application/json").content("{\"prazo\":\"UM_MES\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.funcionarioId").value(funcionario.getId()))
                .andExpect(jsonPath("$.status").value("ATIVO")).andExpect(jsonPath("$.exemplar.tipo").value("FISICO"));
    }

    @Test void funcionarioAtivoPodeEntregarPedidoMasInativoNao() throws Exception {
        admin.setRole(Role.FUNCIONARIO);usuarios.save(admin);
        var criado=mvc.perform(post("/api/minha-conta/emprestimos/"+fisico().getId()).with(user(leitor.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"UMA_SEMANA\"}"))
                .andExpect(status().isOk()).andReturn();
        long id=new tools.jackson.databind.ObjectMapper().readTree(criado.getResponse().getContentAsString()).get("id").asLong();
        String entrega="/api/emprestimos/"+id+"/entregar";
        funcionario.setAtivo(false);funcionarios.save(funcionario);
        mvc.perform(patch(entrega).with(user(admin.getEmail())).with(csrf())).andExpect(status().isForbidden());
        funcionario.setAtivo(true);funcionarios.save(funcionario);
        mvc.perform(patch(entrega).with(user(admin.getEmail())).with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionarioId").value(funcionario.getId()));
        mvc.perform(patch("/api/emprestimos/"+id+"/devolver").with(user(leitor.getEmail())).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(patch("/api/emprestimos/"+id+"/devolver").with(user(admin.getEmail())).with(csrf())).andExpect(status().isOk());
    }
    @Test void prazosObrigatoriosEEntregaPreservaMesEscolhido() throws Exception {
        var exemplar=fisico();String url="/api/minha-conta/emprestimos/"+exemplar.getId();
        for(String corpo:new String[]{"{}","{\"prazo\":null}","{\"prazo\":\"TRES_MESES\"}"})
            mvc.perform(post(url).with(user(leitor.getEmail())).with(csrf()).contentType("application/json").content(corpo)).andExpect(status().isBadRequest());
        var criado=mvc.perform(post(url).with(user(leitor.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\"DOIS_MESES\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.prazo").value("DOIS_MESES")).andReturn();
        long id=new tools.jackson.databind.ObjectMapper().readTree(criado.getResponse().getContentAsString()).get("id").asLong();
        var pedido=emprestimos.findById(id).orElseThrow();pedido.setDataEmprestimo(LocalDate.now().minusDays(5));pedido.setDataExpiracao(LocalDate.now().minusDays(5).plusMonths(2));emprestimos.save(pedido);
        mvc.perform(patch("/api/emprestimos/"+id+"/entregar").with(user(admin.getEmail())).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.dataExpiracao").value(LocalDate.now().plusMonths(2).toString()));
    }
    @Test void digitalAceitaTresPrazosESimultaneidade() throws Exception {
        var exemplar=fisico();exemplar.setTipo(TypeLivro.VIRTUAL);exemplar.setCodigoPatrimonio(null);exemplar.setCaminhoArquivo("pdf_exemplo.pdf");exemplares.save(exemplar);
        for(var prazo:PrazoEmprestimo.values()) {
            var u=usuario(Role.USER);
            mvc.perform(post("/api/minha-conta/emprestimos/"+exemplar.getId()).with(user(u.getEmail())).with(csrf()).contentType("application/json").content("{\"prazo\":\""+prazo.name()+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.dataExpiracao").value(prazo.calcular(LocalDate.now()).toString()));
        }
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
