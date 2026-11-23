package com.libteca.service;

import com.libteca.dto.usuario.SenhaRequest;
import com.libteca.dto.usuario.UsuarioRequest;
import com.libteca.dto.usuario.UsuarioResponse;
import com.libteca.entity.Usuario;
import com.libteca.enums.Role;
import com.libteca.handler.usuario.exception.SenhaIncorretaException;
import com.libteca.handler.usuario.exception.UsuarioJaExisteException;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.UsuarioMapper;
import com.libteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {


    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final AcessoService acesso;
    private final TelefoneService telefones;

    public UsuarioService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, AcessoService acesso, TelefoneService telefones) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.acesso = acesso;
        this.telefones = telefones;
    }

    @org.springframework.transaction.annotation.Transactional
    public UsuarioResponse cadastrarUsuario(com.libteca.dto.usuario.CadastroUsuarioRequest request) {
        var salvo=adicionarUsuario(new UsuarioRequest(request.nome(),request.email(),request.senha()));
        telefones.adicionarTelefone(new com.libteca.dto.telefone.TelefoneRequest(salvo.id(),request.telefone()));
        return salvo;
    }

    // Todos
    public Page<UsuarioResponse> listarTodos(Pageable pageable) {

        return usuarioRepository
                .findAll(pageable)
                .map(usuarioMapper::toResponse);
    }

    // Todos
    public void apagarTodos() {
        if (usuarioRepository.existsByRoleIn(java.util.List.of(Role.ADMIN, Role.ADMIM)))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Administradores não podem ser excluídos. Use Minha biblioteca");
        if (usuarioRepository.existsByRoleIn(java.util.List.of(Role.FUNCIONARIO))) exigirAdministrador();
        usuarioRepository.deleteAll();
    }

    // Adicionar
    public UsuarioResponse adicionarUsuario(UsuarioRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new UsuarioJaExisteException("Já existe um usuário com este email");
        }

        Usuario usuario = usuarioMapper.toEntity(request);

        usuario.setSenha(passwordEncoder.encode(request.senha()));

        usuario.setRole(Role.USER);

        Usuario salvo = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(salvo);
    }

    // Buscar por ID
    public UsuarioResponse mostrarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        return usuarioMapper.toResponse(usuario);
    }

    // Apagar por ID
    public void apagarUsuario(Long id) {

        Usuario usuario=usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        if (usuario.getRole()==Role.ADMIN || usuario.getRole()==Role.ADMIM)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Administradores não podem ser excluídos. Use Minha biblioteca");

        if (usuario.getRole()==Role.FUNCIONARIO) exigirAdministrador();

        usuarioRepository.deleteById(id);
    }

    private void exigirAdministrador() {
        if (!acesso.administrador(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Somente o administrador pode excluir funcionários");
    }

    public UsuarioResponse atualizarUsuario(
            Long id,
            UsuarioRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException(
                                "Usuário não encontrado"
                        ));

        if (usuarioRepository.existsByEmailAndIdNot(
                request.email(),
                id)) {

            throw new UsuarioJaExisteException(
                    "Já existe outro usuário com este email"
            );
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());

        Usuario atualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(atualizado);
    }

    public void alterarSenha(
            Long id,
            SenhaRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException(
                                "Usuário não encontrado"
                        ));

        if (!passwordEncoder.matches(
                request.senhaAtual(),
                usuario.getSenha())) {

            throw new SenhaIncorretaException(
                    "A senha atual está incorreta"
            );
        }

        usuario.setSenha(
                passwordEncoder.encode(request.novaSenha())
        );

        usuarioRepository.save(usuario);
    }

}
