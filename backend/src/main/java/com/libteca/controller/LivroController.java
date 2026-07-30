package com.libteca.controller;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @GetMapping
    public List<LivroResponse> listarTodos() {
        return livroService.listarTodos();
    }

    @GetMapping("/{id}")
    public LivroResponse mostrarLivro(@PathVariable Long id) {
        return livroService.mostrarLivro(id);
    }

    @PostMapping
    public LivroResponse adicionarLivro(@Valid @RequestBody LivroRequest request) {
        return livroService.adicionarLivro(request);
    }

    @DeleteMapping("/{id}")
    public void apagarLivro(@PathVariable Long id) {
        livroService.apagarLivro(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        livroService.apagarTodos();
    }
}