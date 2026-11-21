package com.libteca.service;

import com.libteca.dto.telefone.*;
import com.libteca.entity.*;
import com.libteca.mapper.TelefoneMapper;
import com.libteca.repository.*;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional
public class TelefoneService {

    private final TelefoneRepository telefoneRepository;
    private final UsuarioRepository usuarioRepository;
    private final TelefoneMapper telefoneMapper;

    public TelefoneService(TelefoneRepository telefoneRepository, UsuarioRepository usuarioRepository, TelefoneMapper telefoneMapper) {
        this.telefoneRepository = telefoneRepository;
        this.usuarioRepository = usuarioRepository;
        this.telefoneMapper = telefoneMapper;
    }

    public List<TelefoneResponse> listarTodos() {
        return telefoneRepository.findAll().stream().map(telefoneMapper::toResponse).toList();
    }

    public List<TelefoneResponse> buscarPorUsuario(Long usuarioId) {
        return telefoneRepository.findByUsuarioId(usuarioId).stream().map(telefoneMapper::toResponse).toList();
    }

    public TelefoneResponse mostrarTelefone(Long id) {
        return telefoneMapper.toResponse(buscarTelefone(id));
    }

    private Telefone buscarTelefone(Long id) {
        return telefoneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone não encontrado"));
    }

    public TelefoneResponse adicionarTelefone(TelefoneRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        if (telefoneRepository.existsByNumero(request.numero())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este telefone já está cadastrado");
        }
        return telefoneMapper.toResponse(telefoneRepository.save(telefoneMapper.toEntity(request, usuario)));
    }

    public void apagarTelefone(Long id) {
        telefoneRepository.delete(buscarTelefone(id));
    }

    public void apagarTodos() {
        telefoneRepository.deleteAll();
    }
}
