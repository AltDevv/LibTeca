package com.libteca.service;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.Livro;
import com.libteca.mapper.LivroMapper;
import com.libteca.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService {

    private final LivroMapper livroMapper;
    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository, LivroMapper livroMapper) {
        this.livroRepository = livroRepository;
        this.livroMapper = livroMapper;
    }

    // Todos
    public List<LivroResponse> listarTodos() {
        List<Livro> livros = livroRepository.findAll();

        List<LivroResponse> resposta = livros.stream()
                .map(livro -> livroMapper.toResponse(livro))
                .toList();

        return resposta;
    }

    // Todos
    public void apagarTodos() {
        livroRepository.deleteAll();
    }

    // Adicionar
    public LivroResponse adicionarLivro(LivroRequest request) {

        Livro livro = livroMapper.toEntity(request);

        Livro salvo = livroRepository.save(livro);

        return livroMapper.toResponse(salvo);
    }

    // Buscar por ID
    public LivroResponse mostrarLivro(Long id) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        return livroMapper.toResponse(livro);
    }

    // Apagar por ID
    public void apagarLivro(Long id) {

        if (!livroRepository.existsById(id)) {
            throw new RuntimeException("Livro não encontrado");
        }

        livroRepository.deleteById(id);
    }

}