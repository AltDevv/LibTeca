package com.libteca.service;

import com.libteca.entity.Livro;
import com.libteca.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    //All
    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }
    //All
    public void apagarTodos() {
        livroRepository.deleteAll();
    }

    public Livro adicionarLivro(Livro livro) {
        return livroRepository.save(livro);
    }

    //Esp
    public void apagarLivro(Long id) {
        if (!livroRepository.existsById(id)) {
            throw new RuntimeException("Livro não encontrado");
        }

        livroRepository.deleteById(id);
    }

    //Esp
    public Livro mostrarLivro(Long id) {
        Optional<Livro> livroEncontrado = livroRepository.findById(id);

        if (livroEncontrado.isPresent()) {
            return livroEncontrado.get();
        }

        throw new RuntimeException("Livro não encontrado");
    }



}