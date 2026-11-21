package com.libteca.service;

import com.libteca.handler.livro.exception.AutorNaoEncontradoException;

import com.libteca.dto.autor.AutorRequest;
import com.libteca.dto.autor.AutorResponse;
import com.libteca.entity.Autor;
import com.libteca.mapper.AutorMapper;
import com.libteca.repository.AutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutorService {

    private final AutorMapper autorMapper;
    private final AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository, AutorMapper autorMapper) {
        this.autorRepository = autorRepository;
        this.autorMapper = autorMapper;
    }

    // Todos
    public List<AutorResponse> listarTodos() {
        List<Autor> autores = autorRepository.findAll();

        List<AutorResponse> resposta = autores.stream()
                .map(autor -> autorMapper.toResponse(autor))
                .toList();

        return resposta;
    }

    // Todos
    public void apagarTodos() {
        autorRepository.deleteAll();
    }

    // Adicionar
    public AutorResponse adicionarAutor(AutorRequest request) {

        Autor autor = autorMapper.toEntity(request);

        Autor salvo = autorRepository.save(autor);

        return autorMapper.toResponse(salvo);
    }

    // Buscar por ID
    public AutorResponse mostrarAutor(Long id) {

        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new AutorNaoEncontradoException("Autor não encontrado"));

        return autorMapper.toResponse(autor);
    }

    // Apagar por ID
    public void apagarAutor(Long id) {

        if (!autorRepository.existsById(id)) {
            throw new AutorNaoEncontradoException("Autor não encontrado");
        }

        autorRepository.deleteById(id);
    }

}
