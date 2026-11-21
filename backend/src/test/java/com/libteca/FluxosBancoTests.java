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
    @org.springframework.test.context.DynamicPropertySource
    static void configurarArquivos(org.springframework.test.context.DynamicPropertyRegistry r) throws Exception {
        var pasta=java.nio.file.Files.createTempDirectory("libteca-fluxos-config-");
        r.add("libteca.instalacao.diretorio",()->pasta.toString());
    }
    @Autowired com.libteca.repository.BibliotecaRepository configuracao;

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
    @Autowired VirtualUnicoService virtuais;
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

    @Test void consolidacaoVirtualPreservaHistoricoEIdentificadoresDosEmprestimos() {
        Long original=exemplar(TypeLivro.VIRTUAL);
        jdbc.update("insert into exemplar(idlivro,tipo_exemplar,status_exemplar,data_aquisicao,caminho_arquivo) values (?,'VIRTUAL','DISPONIVEL',?,'livros/teste.pdf')",livroId,LocalDate.now().toString());
        Long duplicado=jdbc.queryForObject("select max(idexemplar) from exemplar where idlivro=?",Long.class,livroId);
        var e=emprestimos.adicionarEmprestimo(pedido(duplicado));
        virtuais.consolidar();
        assertEquals(1,jdbc.queryForObject("select count(*) from exemplar where idlivro=?",Integer.class,livroId));
        assertEquals(original,emprestimos.mostrarEmprestimo(e.id()).exemplar().id());
        assertEquals(1,exemplares.mostrarExemplar(original).emprestimosAtivos());
        virtuais.consolidar();
        assertEquals(e.dataExpiracao(),emprestimos.mostrarEmprestimo(e.id()).dataExpiracao());
    }

    @Test
    void reservasExigemFaltaRespeitamLimiteEFila() {
        Long primeiro=exemplar(TypeLivro.FISICO), segundo=exemplar(TypeLivro.FISICO);
        assertEquals(3,livros.mostrarLivro(livroId).limiteReservas());
        assertThrows(RuntimeException.class,()->reservas.adicionarReserva(new ReservaRequest(primeiro,usuarioId,null)));
        var emprestimo=emprestimos.adicionarEmprestimo(pedido(primeiro));
        assertThrows(RuntimeException.class,()->reservas.adicionarReserva(new ReservaRequest(primeiro,usuarioId,null)));
        emprestimos.adicionarEmprestimo(pedido(segundo));
        var a=reservas.adicionarReserva(new ReservaRequest(primeiro,usuarioId,null));
        for(int i=0;i<2;i++){
            Long leitor=usuarios.adicionarUsuario(new UsuarioRequest("Fila",UUID.randomUUID()+"@teste.local","senhaTeste123")).id();
            reservas.adicionarReserva(new ReservaRequest(primeiro,leitor,null));
        }
        assertEquals(3,livros.mostrarLivro(livroId).reservasEmAndamento());
        Long quarto=usuarios.adicionarUsuario(new UsuarioRequest("Fila",UUID.randomUUID()+"@teste.local","senhaTeste123")).id();
        assertThrows(RuntimeException.class,()->reservas.adicionarReserva(new ReservaRequest(segundo,quarto,null)));
        reservas.cancelarReserva(a.id());
        reservas.adicionarReserva(new ReservaRequest(segundo,quarto,null));
        emprestimos.devolverEmprestimo(emprestimo.id());
        assertEquals(StatusExemplar.RESERVADO,exemplares.mostrarExemplar(primeiro).status());
        var l=livros.mostrarLivro(livroId);
        livros.atualizarLivro(livroId,new LivroRequest(l.titulo(),autorIds,categoriaIds,l.editora().id(),null,l.capa(),l.numeroDePaginas(),l.dataLancamento(),0));
        assertEquals(0,livros.mostrarLivro(livroId).limiteReservas());
        assertEquals(3,livros.mostrarLivro(livroId).reservasEmAndamento());
        assertThrows(RuntimeException.class,()->reservas.adicionarReserva(new ReservaRequest(segundo,usuarioId,null)));
    }

    @Test
    void reservasSimultaneasNaoUltrapassamLimiteDoLivro() throws Exception {
        Long a=exemplar(TypeLivro.FISICO), b=exemplar(TypeLivro.FISICO);
        emprestimos.adicionarEmprestimo(pedido(a));emprestimos.adicionarEmprestimo(pedido(b));
        var ids=new ArrayList<Long>();
        for(int i=0;i<5;i++) ids.add(usuarios.adicionarUsuario(new UsuarioRequest("Fila",UUID.randomUUID()+"@teste.local","senhaTeste123")).id());
        var executor=Executors.newFixedThreadPool(5);var inicio=new CountDownLatch(1);
        try {
            var resultados=new ArrayList<Future<Boolean>>();
            for(int i=0;i<5;i++){
                Long exemplar=i%2==0?a:b, leitor=ids.get(i);
                resultados.add(executor.submit(()->{inicio.await();try{reservas.adicionarReserva(new ReservaRequest(exemplar,leitor,null));return true;}catch(com.libteca.handler.livro.exception.LivroIndisponivelException erro){return false;}}));
            }
            inicio.countDown();int aceitas=0;
            for(var resultado:resultados)if(resultado.get(20,TimeUnit.SECONDS))aceitas++;
            assertEquals(3,aceitas);assertEquals(3,livros.mostrarLivro(livroId).reservasEmAndamento());
        } finally {executor.shutdownNow();}
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
        assertEquals(1,configuracao.findById(1L).orElseThrow().getAcervoIniciado());
        livros.apagarLivro(livroId);
        assertEquals(1,configuracao.findById(1L).orElseThrow().getAcervoIniciado());
    }

    @Test
    void emprestimoFisicoAguardaRecebimentoEIniciaPrazoNaConfirmacao() {
        Long id = exemplar(TypeLivro.FISICO);
        var pedido = emprestimos.solicitarEmprestimoFisico(pedido(id));
        assertEquals(StatusEmprestimo.PENDENTE, pedido.status());
        assertEquals(StatusExemplar.RESERVADO, exemplares.mostrarExemplar(id).status());
        assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimo(pedido(id)));
        assertThrows(RuntimeException.class, () -> emprestimos.devolverEmprestimo(pedido.id()));
        var reserva = reservas.adicionarReserva(new ReservaRequest(id, usuarioId, null));
        lancamentos.processarReservasDeLancamento();
        assertEquals(StatusReserva.ATIVA, reservas.mostrarReserva(reserva.id()).status());
        assertEquals(StatusExemplar.RESERVADO, exemplares.mostrarExemplar(id).status());
        jdbc.update("update emprestimo set data_emprestimo=?,data_expiracao=? where idemprestimo=?",
                LocalDate.now().minusDays(20).toString(), LocalDate.now().minusDays(6).toString(), pedido.id());
        emprestimos.processarEmprestimosExpirados();
        assertEquals(StatusEmprestimo.PENDENTE, emprestimos.mostrarEmprestimo(pedido.id()).status());
        var recebido = emprestimos.confirmarRecebimento(pedido.id(), funcionarioId);
        assertEquals(StatusEmprestimo.ATIVO, recebido.status());
        assertEquals(LocalDate.now(), recebido.dataEmprestimo());
        assertEquals(LocalDate.now().plusDays(14), recebido.dataExpiracao());
        assertEquals(StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(id).status());
        assertThrows(RuntimeException.class, () -> emprestimos.confirmarRecebimento(pedido.id(), funcionarioId));
        assertThrows(RuntimeException.class, () -> emprestimos.cancelarSolicitacao(pedido.id()));
        emprestimos.devolverEmprestimo(pedido.id());
        assertEquals(StatusReserva.DISPONIVEL_PARA_RETIRADA, reservas.mostrarReserva(reserva.id()).status());
    }

    @Test
    void cancelarPedidoFisicoLiberaExemplarSemRegistrarDevolucao() {
        Long id = exemplar(TypeLivro.FISICO);
        var pedido = emprestimos.solicitarEmprestimoFisico(pedido(id));
        var cancelado = emprestimos.cancelarSolicitacao(pedido.id());
        assertEquals(StatusEmprestimo.CANCELADO, cancelado.status());
        assertNull(cancelado.dataDevolucao());
        assertEquals(StatusExemplar.DISPONIVEL, exemplares.mostrarExemplar(id).status());
        assertThrows(RuntimeException.class, () -> emprestimos.confirmarRecebimento(pedido.id(), funcionarioId));
        assertThrows(RuntimeException.class, () -> emprestimos.devolverEmprestimo(pedido.id()));
        var outroPedido = emprestimos.solicitarEmprestimoFisico(pedido(id));
        assertEquals(StatusEmprestimo.PENDENTE, outroPedido.status());
        var reserva = reservas.adicionarReserva(new ReservaRequest(id, usuarioId, null));
        emprestimos.cancelarSolicitacao(outroPedido.id());
        assertEquals(StatusReserva.DISPONIVEL_PARA_RETIRADA, reservas.mostrarReserva(reserva.id()).status());
        assertThrows(RuntimeException.class, () -> emprestimos.solicitarEmprestimoFisico(pedido(exemplar(TypeLivro.VIRTUAL))));
    }

    @Test
    void confirmarECancelarAoMesmoTempoNaoDuplicaATransicao() throws Exception {
        Long id = exemplar(TypeLivro.FISICO);
        var pedido = emprestimos.solicitarEmprestimoFisico(pedido(id));
        var executor = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        try {
            var confirmar = executor.submit(() -> {
                inicio.await();
                try { emprestimos.confirmarRecebimento(pedido.id(), funcionarioId); return true; }
                catch (org.springframework.web.server.ResponseStatusException erro) { assertEquals(409, erro.getStatusCode().value()); return false; }
            });
            var cancelar = executor.submit(() -> {
                inicio.await();
                try { emprestimos.cancelarSolicitacao(pedido.id()); return true; }
                catch (org.springframework.web.server.ResponseStatusException erro) { assertEquals(409, erro.getStatusCode().value()); return false; }
            });
            inicio.countDown();
            assertNotEquals(confirmar.get(20,TimeUnit.SECONDS), cancelar.get(20,TimeUnit.SECONDS));
            var resultado = emprestimos.mostrarEmprestimo(pedido.id());
            assertNull(resultado.dataDevolucao());
            assertEquals(resultado.status()==StatusEmprestimo.ATIVO?StatusExemplar.EMPRESTADO:StatusExemplar.DISPONIVEL,
                    exemplares.mostrarExemplar(id).status());
        } finally { executor.shutdownNow(); }
    }

    @Test
    void emprestaEDevolveFisicoEVirtual() {
        for (TypeLivro tipo : TypeLivro.values()) {
            Long exemplarId = exemplar(tipo);
            var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
            assertEquals(LocalDate.now().plusDays(7), emprestimo.dataExpiracao());
            assertNull(emprestimo.dataDevolucao());
            assertEquals(exemplares.mostrarExemplar(exemplarId).tipo()==TypeLivro.VIRTUAL?StatusExemplar.DISPONIVEL:StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(exemplarId).status());
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
        assertEquals(exemplares.mostrarExemplar(exemplarId).tipo()==TypeLivro.VIRTUAL?StatusExemplar.DISPONIVEL:StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(exemplarId).status());
        assertThrows(RuntimeException.class, () -> emprestimos.apagarEmprestimo(emprestimo.id()));
        assertNotNull(emprestimos.adicionarEmprestimo(pedido(exemplarId)));
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
        Long exemplarId = exemplar(TypeLivro.FISICO);
        var emprestimo = emprestimos.adicionarEmprestimo(pedido(exemplarId));
        var reserva = reservas.adicionarReserva(new ReservaRequest(exemplarId, usuarioId, funcionarioId));
        reservas.cancelarReserva(reserva.id());
        assertEquals(exemplares.mostrarExemplar(exemplarId).tipo()==TypeLivro.VIRTUAL?StatusExemplar.DISPONIVEL:StatusExemplar.EMPRESTADO, exemplares.mostrarExemplar(exemplarId).status());
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
        var digital = exemplares.adicionarExemplar(new ExemplarRequest(livroId, TypeLivro.VIRTUAL, LocalDate.now(), null, null));
        assertEquals(StatusExemplar.INDISPONIVEL, digital.status());
        assertNull(digital.caminhoArquivo());
    }

    @Test
    void umVirtualAtendeVariosLeitoresSemConsumirDisponibilidade() throws Exception {
        var digitais=exemplares.adicionarVirtuais(new ExemplarRequest(livroId,TypeLivro.VIRTUAL,LocalDate.now(),"livros/teste.pdf",null));
        assertEquals(1,digitais.size());Long id=digitais.get(0).id();
        assertThrows(RuntimeException.class,()->exemplares.adicionarVirtuais(new ExemplarRequest(livroId,TypeLivro.VIRTUAL,LocalDate.now(),"livros/teste.pdf",null)));
        var leitores=new ArrayList<Long>();
        for(int i=0;i<6;i++)leitores.add(usuarios.adicionarUsuario(new UsuarioRequest("Digital",UUID.randomUUID()+"@teste.local","senhaTeste123")).id());
        var executor=Executors.newFixedThreadPool(6);var inicio=new CountDownLatch(1);
        try {
            var resultados=new ArrayList<Future<Long>>();
            for(Long leitor:leitores)resultados.add(executor.submit(()->{inicio.await();return emprestimos.adicionarEmprestimoDigital(new EmprestimoRequest(id,leitor,null,LocalDate.now().plusDays(14))).id();}));
            inicio.countDown();var criados=new ArrayList<Long>();
            for(var r:resultados)criados.add(r.get(30,TimeUnit.SECONDS));
            assertEquals(6,exemplares.mostrarExemplar(id).emprestimosAtivos());
            assertEquals(6,exemplares.mostrarExemplar(id).totalEmprestimos());
            assertEquals(StatusExemplar.DISPONIVEL,exemplares.mostrarExemplar(id).status());
            assertThrows(RuntimeException.class,()->emprestimos.adicionarEmprestimoDigital(new EmprestimoRequest(id,leitores.get(0),null,LocalDate.now().plusDays(14))));
            emprestimos.devolverEmprestimo(criados.get(0));
            assertEquals(5,exemplares.mostrarExemplar(id).emprestimosAtivos());
            assertEquals(6,exemplares.mostrarExemplar(id).totalEmprestimos());
            assertThrows(RuntimeException.class,()->exemplares.atualizarExemplar(id,new ExemplarRequest(livroId,TypeLivro.VIRTUAL,LocalDate.now(),"livros/outro.pdf",null)));
        } finally {executor.shutdownNow();}
    }
    @Test
    void digitalSemArquivoNaoPodeSerEmprestadoEAceitaArquivoDepois() {
        var digitais = exemplares.adicionarVirtuais(new ExemplarRequest(livroId, TypeLivro.VIRTUAL,
                LocalDate.now(), null, null));
        assertTrue(digitais.stream().allMatch(e -> e.status() == StatusExemplar.INDISPONIVEL));
        Long id = digitais.get(0).id();
        assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimoDigital(pedido(id)));
        assertThrows(RuntimeException.class, () -> emprestimos.adicionarEmprestimo(pedido(id)));
        var editado = exemplares.atualizarExemplar(id, new ExemplarRequest(livroId, TypeLivro.VIRTUAL,
                LocalDate.now(), "livros/teste.pdf", null));
        assertEquals(StatusExemplar.DISPONIVEL, editado.status());
        assertNotNull(emprestimos.adicionarEmprestimoDigital(pedido(id)).id());
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
