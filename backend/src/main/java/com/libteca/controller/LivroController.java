package com.libteca.controller;

import  com.libteca.entity.Livro;
import com.libteca.service.LivroService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @GetMapping
    public List<Livro> listarTodos(){
        return livroService.listarTodos();
    }

    @GetMapping("/{id}")
    public Livro mostrarLivro(@PathVariable Long id){
        return livroService.mostrarLivro(id);
    }

    @PostMapping
    public Livro adicionarLivro(@Valid @RequestBody Livro livro){
        return livroService.adicionarLivro(livro);
    }

    @DeleteMapping("/{id}")
    public void apagarLivro(@PathVariable Long id){
        livroService.apagarLivro(id);
    }

    @DeleteMapping
    public void apagarTodos(){
        livroService.apagarTodos();
    }

}
