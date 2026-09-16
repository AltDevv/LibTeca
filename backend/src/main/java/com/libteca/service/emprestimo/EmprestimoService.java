package com.libteca.service.emprestimo;

import com.libteca.dto.emprestimo.EmprestimoRequest;
import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.entity.Emprestimo;
import com.libteca.entity.Livro;
import com.libteca.entity.Usuario;
import com.libteca.enums.TypeLivro;
import com.libteca.handler.emprestimo.exception.EmprestimoJaDevolvidoException;
import com.libteca.handler.emprestimo.exception.EmprestimoNaoEncontradoException;
import com.libteca.handler.emprestimo.exception.EmprestimoVirtualException;
import com.libteca.handler.livro.exception.LivroIndisponivelException;
import com.libteca.handler.livro.exception.LivroNaoEncontradoException;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.EmprestimoMapper;
import com.libteca.repository.EmprestimoRepository;
import com.libteca.repository.LivroRepository;
import com.libteca.service.emprestimo.tipoEmprestimo.EmprestimoFisicoService;
import com.libteca.repository.UsuarioRepository;
import com.libteca.service.emprestimo.tipoEmprestimo.EmprestimoVirtualService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class EmprestimoService {

    private final EmprestimoMapper emprestimoMapper;
    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoFisicoService emprestimoFisicoService;
    private final EmprestimoVirtualService emprestimoVirtualService;

    public EmprestimoService(EmprestimoRepository emprestimoRepository, EmprestimoMapper emprestimoMapper,
                             LivroRepository livroRepository, UsuarioRepository usuarioRepository, EmprestimoFisicoService
                                     emprestimoFisicoService, EmprestimoVirtualService emprestimoVirtualService) {
        this.emprestimoRepository = emprestimoRepository;
        this.emprestimoMapper = emprestimoMapper;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoFisicoService = emprestimoFisicoService;
        this.emprestimoVirtualService = emprestimoVirtualService;
    }

    // Todos
    public Page<EmprestimoResponse> listarTodos(Pageable pageable) {
        return emprestimoRepository
                .findAll(pageable)
                .map(emprestimoMapper::toResponse);
    }

    // Todos
    public void apagarTodos() {
        emprestimoRepository.deleteAll();
    }

    // Adicionar
    @Transactional
    public EmprestimoResponse adicionarEmprestimo(EmprestimoRequest request) {

        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));


        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        LocalDate dataExpiracao = null;

        if (livro.getTipo() == TypeLivro.FISICO){
            emprestimoFisicoService.emprestarFisico(livro);
        }

        //Caso tenha o livro e usuário se verifica primeiro se é virtual pois um livro virtual não tem limites
        else if (livro.getTipo() == TypeLivro.VIRTUAL){
            dataExpiracao =
                    emprestimoVirtualService.emprestarVirtual(livro);
        }

        Emprestimo emprestimo = emprestimoMapper.toEntity(request, livro, usuario, dataExpiracao);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        return emprestimoMapper.toResponse(salvo);
    }

    // Buscar por ID
    public EmprestimoResponse mostrarEmprestimo(Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException("Empréstimo não encontrado"));

        return emprestimoMapper.toResponse(emprestimo);
    }

    // Registrar devolução
    @Transactional
    public EmprestimoResponse devolverEmprestimo(Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException("Empréstimo não encontrado"));

        if (emprestimo.getDataDevolucao() != null) {
            throw new EmprestimoJaDevolvidoException("Este empréstimo já foi devolvido");
        }

        Livro livro = emprestimo.getLivro();

        if (livro.getTipo() == TypeLivro.VIRTUAL) {
            throw new EmprestimoVirtualException(
                    "Livros virtuais não podem ser devolvidos"
            );
        }
        //Não sendo virtual, logo é físico.
        emprestimoFisicoService.devolverFisico(livro);

        emprestimo.setDataDevolucao(LocalDate.now());

        livroRepository.save(livro);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        return emprestimoMapper.toResponse(salvo);
    }

    // Apagar por ID
    public void apagarEmprestimo(Long id) {

        if (!emprestimoRepository.existsById(id)) {
            throw new EmprestimoNaoEncontradoException("Empréstimo não encontrado");
        }

        emprestimoRepository.deleteById(id);
    }

}
