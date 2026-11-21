package com.libteca.service;

import com.libteca.dto.funcionario.*;
import com.libteca.entity.*;
import com.libteca.enums.Role;
import com.libteca.mapper.FuncionarioMapper;
import com.libteca.repository.*;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final BibliotecaRepository bibliotecaRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, UsuarioRepository usuarioRepository,
                              FuncionarioMapper funcionarioMapper, BibliotecaRepository bibliotecaRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioMapper = funcionarioMapper;
        this.bibliotecaRepository = bibliotecaRepository;
    }

    public List<FuncionarioResponse> listarTodos() {
        return funcionarioRepository.findAll().stream().map(funcionarioMapper::toResponse).toList();
    }

    public FuncionarioResponse mostrarFuncionario(Long id) {
        return funcionarioMapper.toResponse(buscarFuncionario(id));
    }

    public Funcionario buscarFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    public Funcionario buscarAtivo(Long id) {
        if (id == null) return null;
        Funcionario funcionario = buscarFuncionario(id);
        if (!Boolean.TRUE.equals(funcionario.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O funcionário está inativo");
        }
        return funcionario;
    }

    public FuncionarioResponse adicionarFuncionario(FuncionarioRequest request) {
        bibliotecaRepository.bloquear();
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        if (funcionarioRepository.existsByUsuarioId(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuário já possui cadastro de funcionário");
        }
        if (usuario.getRole() == Role.USER) usuario.setRole(Role.FUNCIONARIO);
        Funcionario funcionario = funcionarioMapper.toEntity(request, usuario);
        return funcionarioMapper.toResponse(funcionarioRepository.save(funcionario));
    }

    public FuncionarioResponse atualizarFuncionario(Long id, FuncionarioRequest request) {
        Funcionario funcionario = buscarFuncionario(id);
        if (!funcionario.getUsuario().getId().equals(request.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é permitido trocar o usuário do funcionário");
        }
        if (!request.ativo() && administrador(funcionario))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfira a administração antes de desativar esta conta");
        funcionario.setDataAdmissao(request.dataAdmissao());
        funcionario.setAtivo(request.ativo());
        return funcionarioMapper.toResponse(funcionario);
    }

    public void apagarFuncionario(Long id) {
        Funcionario funcionario = buscarFuncionario(id);
        if (administrador(funcionario))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Administradores não podem ser excluídos. Use Minha biblioteca");
        if (funcionario.getUsuario().getRole() == Role.FUNCIONARIO) funcionario.getUsuario().setRole(Role.USER);
        funcionarioRepository.delete(funcionario);
        funcionarioRepository.flush();
    }

    public void apagarTodos() {
        if (funcionarioRepository.findAll().stream().anyMatch(this::administrador))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Administradores não podem ser excluídos. Use Minha biblioteca");
        for (Funcionario funcionario : funcionarioRepository.findAll()) apagarFuncionario(funcionario.getId());
    }
    private boolean administrador(Funcionario f) {
        return f.getUsuario().getRole()==Role.ADMIN || f.getUsuario().getRole()==Role.ADMIM;
    }
}
