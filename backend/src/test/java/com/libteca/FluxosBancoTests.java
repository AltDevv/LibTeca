package com.libteca;

import com.libteca.dto.autor.AutorRequest;
import com.libteca.dto.categoria.CategoriaRequest;
import com.libteca.dto.editora.EditoraRequest;
import com.libteca.dto.usuario.UsuarioRequest;
import com.libteca.dto.funcionario.FuncionarioRequest;
import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.exemplar.ExemplarRequest;
import com.libteca.dto.emprestimo.EmprestimoRequest;
import com.libteca.dto.reserva.*;
import com.libteca.dto.telefone.TelefoneRequest;
import com.libteca.enums.*;
import com.libteca.filter.LivroFiltro;
import com.libteca.service.*;
import com.libteca.service.emprestimo.EmprestimoService;
import com.libteca.service.reserva.*;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class FluxosBancoTests {

    @Autowired UsuarioService usuarios;
    @Autowired FuncionarioService funcionarios;
    @Autowired LivroService livros;
    @Autowired AutorService autores;
    @Autowired CategoriaService categorias;
    @Autowired EditoraService editoras;
    @Autowired ExemplarService exemplares;
    @Autowired TelefoneService telefones;
    @Autowired EmprestimoService emprestimos;
    @Autowired ReservaService reservas;
    @Autowired ReservaLancamentoService lancamentos;
    @Autowired JdbcTemplate jdbc;
    @Autowired Validator validator;
    @Autowired org.springframework.web.context.WebApplicationContext contexto;
    Long usuarioId;
    Long funcionarioId;
    Long livroId;
    Set<Long> autorIds;
    Set<Long> categoriaIds;

    @BeforeEach
    void preparar() {
        String sufixo = UUID.randomUUID().toString();
        usuarioId = usuarios.adicionarUsuario(new UsuarioRequest("Leitor", sufixo + "@teste.local", "senhaTeste123")).id();
        Long conta = usuarios.adicionarUsuario(new UsuarioRequest("Atendente", "f" + sufixo + "@teste.local", "senhaTeste123")).id();
        funcionarioId = funcionarios.adicionarFuncionario(new FuncionarioRequest(conta, LocalDate.now(), true)).id();
        autorIds = Set.of(autores.adicionarAutor(new AutorRequest("Autor um")).id(), autores.adicionarAutor(new AutorRequest("Autor dois")).id());
        categoriaIds = Set.of(categorias.adicionarCategoria(new CategoriaRequest("Categoria um")).id(), categorias.adicionarCategoria(new CategoriaRequest("Categoria dois")).id());
        Long editoraId = editoras.adicionarEditora(new EditoraRequest("Editora teste")).id();
        livroId = livros.adicionarLivro(new LivroRequest("Livro " + sufixo, autorIds, categoriaIds, editoraId,
                funcionarioId, null, 120, LocalDate.now().minusYears(1))).id();
    }

    Long exemplar(TypeLivro tipo) {
        return exemplares.adicionarExemplar(new ExemplarRequest(livroId, tipo, LocalDate.now(),
                tipo == TypeLivro.VIRTUAL ? "livros/teste.pdf" : null,
                tipo == TypeLivro.FISICO ? UUID.randomUUID().toString() : null)).id();
    }

    EmprestimoRequest pedido(Long exemplarId) {
        return new EmprestimoRequest(exemplarId, usuarioId, funcionarioId, LocalDate.now().plusDays(7));
    }

    @Test
    void editaLivroEExemplarPreservandoResponsavelStatusEHistorico() {
        var livro=livros.mostrarLivro(livroId);
        funcionarios.atualizarFuncionario(funcionarioId, new FuncionarioRequest(
                funcionarios.mostrarFuncionario(funcionarioId).usuario().id(),LocalDate.now(),false));
        var editado=livros.atualizarLivro(livroId,new LivroRequest("Título editado",autorIds,categoriaIds,
                livro.editora().id(),null,"/capas/teste.jpg",200,livro.dataLancamento()));
        assertEquals("Título editado",editado.titulo());assertEquals(funcionarioId,editado.funcionarioId());
        funcionarios.atualizarFuncionario(funcionarioId, new FuncionarioRequest(
                funcionarios.mostrarFuncionario(funcionarioId).usuario().id(),LocalDate.now(),true));
        Long id=exemplar(TypeLivro.FISICO);
        var original=exemplares.mostrarExemplar(id);
        var pedidoEdicao=new ExemplarRequest(livroId,TypeLivro.FISICO,LocalDate.now().minusDays(2),null,original.codigoPatrimonio());
        var copia=exemplares.atualizarExemplar(id,pedidoEdicao);
        assertEquals(StatusExemplar.DISPONIVEL,copia.status());
        assertEquals(LocalDate.now().minusDays(2),copia.dataAquisicao());
        var emprestimo=emprestimos.adicionarEmprestimo(pedido(id));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->exemplares.atualizarExemplar(id,pedidoEdicao));
        emprestimos.devolverEmprestimo(emprestimo.id());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->exemplares.atualizarExemplar(id,
                new ExemplarRequest(livroId,TypeLivro.VIRTUAL,LocalDate.now(),"livros/teste.pdf",null)));
        assertEquals(TypeLivro.FISICO,exemplares.mostrarExemplar(id).tipo());
    }

    @Test
    void primeiroLivroDesativaExemplosMesmoDepoisDaExclusao() {
        assertEquals(1,jdbc.queryForObject("select acervo_iniciado from configuracao_biblioteca where id=1",Integer.class));
        livros.apagarLivro(livroId);
        assertEquals(1,jdbc.queryForObject("select acervo_iniciado from configuracao_biblioteca where id=1",Integer.class));
    }

    @Test
    void emprestaEDevolveFisicoEVirtual() {
        for (TypeLivro tipo : TypeLivro.values()) {
            Long exemplarId = exemplar(tipo);
            var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
            assertEquals(LocalDate.now().plusDays(7), emprestimo.dataExpiracao());
            assertNull(emprestimo.dataDevolucao());
            assertEquals(StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(exemplarId).status());
            assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimo(pedido(exemplarId)));
            var devolvido = emprestimos.devolverEmprestimo(emprestimo.id());
            assertEquals(StatusEmprestimo.DEVOLVIDO, devolvido.status());
            assertEquals(LocalDate.now(), devolvido.dataDevolucao());
            assertEquals(StatusExemplar.DISPONIVEL, exemplares.mostrarExemplar(exemplarId).status());
            assertThrows(RuntimeException.class, () -> emprestimos.devolverEmprestimo(emprestimo.id()));
        }
    }

    @Test
    void prazoObrigatorioEVencimentoNaoLiberaExemplar() {
        Long exemplarId = exemplar(TypeLivro.VIRTUAL);
        assertThrows(IllegalArgumentException.class, () -> emprestimos.adicionarEmprestimo(new EmprestimoRequest(exemplarId, usuarioId, null, null)));
        assertThrows(IllegalArgumentException.class, () -> emprestimos.adicionarEmprestimo(new EmprestimoRequest(exemplarId, usuarioId, null, LocalDate.now().minusDays(1))));
        var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
        jdbc.update("update emprestimo set data_expiracao = ? where idemprestimo = ?", LocalDate.now().minusDays(1).toString(), emprestimo.id());
        emprestimos.processarEmprestimosExpirados();
        assertEquals(StatusEmprestimo.EXPIRADO, emprestimos.mostrarEmprestimo(emprestimo.id()).status());
        assertEquals(StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(exemplarId).status());
        assertThrows(RuntimeException.class, () -> emprestimos.apagarEmprestimo(emprestimo.id()));
        assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimo(pedido(exemplarId)));
        assertEquals(StatusEmprestimo.DEVOLVIDO, emprestimos.devolverEmprestimo(emprestimo.id()).status());
    }

    @Test
    void reservaDeExemplarEmprestadoAguardaDevolucaoERetiradaExigePrazo() {
        Long exemplarId = exemplar(TypeLivro.FISICO);
        var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
        var reserva = reservas.adicionarReserva(new ReservaRequest(exemplarId, usuarioId, null));
        assertEquals(StatusReserva.ATIVA, reserva.status());
        assertThrows(RuntimeException.class, () -> reservas.adicionarReserva(new ReservaRequest(exemplarId, usuarioId, null)));
        assertThrows(RuntimeException.class, () -> reservas.retirarReserva(reserva.id(), new RetiradaReservaRequest(LocalDate.now().plusDays(5), null)));
        emprestimos.devolverEmprestimo(emprestimo.id());
        assertEquals(StatusReserva.DISPONIVEL_PARA_RETIRADA, reservas.mostrarReserva(reserva.id()).status());
        assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimo(pedido(exemplarId)));
        assertThrows(IllegalArgumentException.class, () -> reservas.retirarReserva(reserva.id(), new RetiradaReservaRequest(null, null)));
        var retirada = reservas.retirarReserva(reserva.id(), new RetiradaReservaRequest(LocalDate.now().plusDays(5), funcionarioId));
        assertEquals(StatusReserva.CONCLUIDA, reservas.mostrarReserva(reserva.id()).status());
        assertEquals(LocalDate.now().plusDays(5), retirada.dataExpiracao());
        assertThrows(RuntimeException.class, () -> reservas.cancelarReserva(reserva.id()));
    }

    @Test
    void cancelaReservaSemLiberarEmprestimoECuidaDoPreLancamento() {
        Long exemplarId = exemplar(TypeLivro.VIRTUAL);
        var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
        var reserva = reservas.adicionarReserva(new ReservaRequest(exemplarId, usuarioId, funcionarioId));
        reservas.cancelarReserva(reserva.id());
        assertEquals(StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(exemplarId).status());
        emprestimos.devolverEmprestimo(emprestimo.id());
        jdbc.update("update livro set data_lancamento = ? where idlivro = ?", LocalDate.now().plusDays(1).toString(), livroId);
        var futura = reservas.adicionarReserva(new ReservaRequest(exemplarId, usuarioId, null));
        assertEquals(StatusReserva.ATIVA, futura.status());
        jdbc.update("update livro set data_lancamento = ? where idlivro = ?", LocalDate.now().toString(), livroId);
        lancamentos.processarReservasDeLancamento();
        assertEquals(StatusReserva.DISPONIVEL_PARA_RETIRADA, reservas.mostrarReserva(futura.id()).status());
        reservas.cancelarReserva(futura.id());
        assertEquals(StatusExemplar.DISPONIVEL, exemplares.mostrarExemplar(exemplarId).status());
    }

    @Test
    void associaVariosAutoresECategoriasSemDuplicarLivroNaBusca() {
        assertEquals(2, livros.mostrarLivro(livroId).autores().size());
        assertEquals(2, livros.mostrarLivro(livroId).categorias().size());
        LivroFiltro filtro = new LivroFiltro();
        filtro.setTitulo(livros.mostrarLivro(livroId).titulo());
        filtro.setAutor("Autor");
        filtro.setCategoria("Categoria");
        filtro.setQuantidadeDisponivel(0);
        assertEquals(1, livros.buscar(filtro, PageRequest.of(0, 10)).getTotalElements());
        exemplar(TypeLivro.FISICO);
        filtro.setQuantidadeDisponivel(1);
        filtro.setTipoLivro(TypeLivro.FISICO);
        assertEquals(1, livros.buscar(filtro, PageRequest.of(0, 10)).getTotalElements());
        assertThrows(RuntimeException.class, () -> jdbc.update("insert into livro_autor (idlivro,idautor) values (?,?)", livroId, autorIds.iterator().next()));
        assertThrows(RuntimeException.class, () -> jdbc.update("insert into livro_categoria (idlivro,idcategoria) values (?,?)", livroId, categoriaIds.iterator().next()));
        var erro = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> livros.apagarLivro(livroId));
        assertEquals(409, erro.getStatusCode().value());
        assertTrue(erro.getReason().contains("exemplares vinculados"));
        assertEquals(2, livros.mostrarLivro(livroId).autores().size());
    }

    @Test
    void validaFuncionarioTelefoneETiposDeExemplar() {
        var funcionario = funcionarios.mostrarFuncionario(funcionarioId);
        assertEquals(Role.FUNCIONARIO, funcionario.usuario().role());
        assertThrows(RuntimeException.class, () -> funcionarios.adicionarFuncionario(new FuncionarioRequest(funcionario.usuario().id(), LocalDate.now(), true)));
        Long exemplarId = exemplar(TypeLivro.FISICO);
        funcionarios.atualizarFuncionario(funcionarioId, new FuncionarioRequest(funcionario.usuario().id(), LocalDate.now(), false));
        assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimo(pedido(exemplarId)));
        String numero = "11" + String.format("%09d", usuarioId);
        telefones.adicionarTelefone(new TelefoneRequest(usuarioId, numero));
        assertThrows(RuntimeException.class, () -> telefones.adicionarTelefone(new TelefoneRequest(usuarioId, numero)));
        assertEquals(1, telefones.buscarPorUsuario(usuarioId).size());
        assertFalse(validator.validate(new TelefoneRequest(usuarioId, "abc")).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> exemplares.adicionarExemplar(new ExemplarRequest(livroId, TypeLivro.VIRTUAL, LocalDate.now(), null, null)));
    }

    @Test
    void duasSolicitacoesSimultaneasNaoEmprestamOMesmoExemplar() throws Exception {
        Long exemplarId = exemplar(TypeLivro.FISICO);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        Callable<Boolean> tentativa = () -> {
            inicio.await();
            try {
                emprestimos.adicionarEmprestimo(pedido(exemplarId));
                return true;
            } catch (RuntimeException exception) {
                return false;
            }
        };
        try {
            Future<Boolean> primeira = executor.submit(tentativa);
            Future<Boolean> segunda = executor.submit(tentativa);
            inicio.countDown();
            int sucessos = (primeira.get(20, TimeUnit.SECONDS) ? 1 : 0) + (segunda.get(20, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, sucessos);
            assertEquals(1, jdbc.queryForObject("select count(*) from emprestimo where idexemplar = ? and data_devolucao is null", Integer.class, exemplarId));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void apiValidaPrazoERetornaConflitoSemErroInterno() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(contexto).build();
        Long exemplarId = exemplar(TypeLivro.VIRTUAL);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/emprestimos")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"exemplarId\":" + exemplarId + ",\"usuarioId\":" + usuarioId + "}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/emprestimos/" + emprestimo.id() + "/devolver"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("DEVOLVIDO"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/emprestimos/" + emprestimo.id() + "/devolver"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isConflict());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/exemplares/" + exemplarId))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.livro.autores.length()").value(2));
    }
}
