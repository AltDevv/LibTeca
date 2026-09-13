package com.libteca.service;

import com.libteca.dto.usuario.SenhaRequest;
import com.libteca.dto.usuario.UsuarioRequest;
import com.libteca.dto.usuario.UsuarioResponse;
import com.libteca.entity.Usuario;
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

    public UsuarioService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    // Todos
    public Page<UsuarioResponse> listarTodos(Pageable pageable) {

        return usuarioRepository
                .findAll(pageable)
                .map(usuarioMapper::toResponse);
    }

    // Todos
    public void apagarTodos() {
        usuarioRepository.deleteAll();
    }

    // Adicionar
    public UsuarioResponse adicionarUsuario(UsuarioRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new UsuarioJaExisteException("Já existe um usuário com este email");
        }

        Usuario usuario = usuarioMapper.toEntity(request);

        usuario.setSenha(passwordEncoder.encode(request.senha()));

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

        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNaoEncontradoException("Usuário não encontrado");
        }

        usuarioRepository.deleteById(id);
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
        usuario.setRole(request.role());

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
