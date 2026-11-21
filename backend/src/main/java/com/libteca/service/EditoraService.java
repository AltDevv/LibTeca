package com.libteca.service;

import com.libteca.handler.livro.exception.EditoraNaoEncontradaException;

import com.libteca.dto.editora.EditoraRequest;
import com.libteca.dto.editora.EditoraResponse;
import com.libteca.entity.Editora;
import com.libteca.mapper.EditoraMapper;
import com.libteca.repository.EditoraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EditoraService {

    private final EditoraMapper editoraMapper;
    private final EditoraRepository editoraRepository;

    public EditoraService(EditoraRepository editoraRepository, EditoraMapper editoraMapper) {
        this.editoraRepository = editoraRepository;
        this.editoraMapper = editoraMapper;
    }

    // Todos
    public List<EditoraResponse> listarTodos() {
        List<Editora> editoras = editoraRepository.findAll();

        List<EditoraResponse> resposta = editoras.stream()
                .map(editora -> editoraMapper.toResponse(editora))
                .toList();

        return resposta;
    }

    // Todos
    public void apagarTodos() {
        editoraRepository.deleteAll();
    }

    // Adicionar
    public EditoraResponse adicionarEditora(EditoraRequest request) {

        Editora editora = editoraMapper.toEntity(request);

        Editora salvo = editoraRepository.save(editora);

        return editoraMapper.toResponse(salvo);
    }

    // Buscar por ID
    public EditoraResponse mostrarEditora(Long id) {

        Editora editora = editoraRepository.findById(id)
                .orElseThrow(() -> new EditoraNaoEncontradaException("Editora não encontrada"));

        return editoraMapper.toResponse(editora);
    }

    // Apagar por ID
    public void apagarEditora(Long id) {

        if (!editoraRepository.existsById(id)) {
            throw new EditoraNaoEncontradaException("Editora não encontrada");
        }

        editoraRepository.deleteById(id);
    }

}
