package com.libteca.controller;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.Livro;
import com.libteca.filter.LivroFiltro;
import com.libteca.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<LivroResponse> listarTodos(Pageable pageable) {
            return livroService.listarTodos(pageable);
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


    //!Filtrar busca de livro
    @GetMapping("/buscar")
    public Page<LivroResponse> buscar(
            LivroFiltro filtro,
            Pageable pageable) {

        return livroService.buscar(filtro, pageable);
    }

    //@GetMapping("/buscar/titulo")
    //    public Page<LivroResponse> buscarPorTitulo(
    //            @RequestParam String titulo,
    //            Pageable pageable) {
    //
    //        return livroService.buscarPorTitulo(titulo, pageable);
    //
    //    }
    //
    //    @GetMapping("/buscar/autor")
    //    public Page<LivroResponse> buscarPorAutor(
    //            @RequestParam String autor,
    //            Pageable pageable) {
    //
    //        return livroService.buscarPorAutor(autor, pageable);
    //
    //    }
    //
    //    @GetMapping("/buscar/editora")
    //    public Page<LivroResponse> buscarPorEditora(
    //            @RequestParam String editora,
    //            Pageable pageable) {
    //
    //        return livroService.buscarPorEditora(editora, pageable);
    //
    //    }
    //
    //    @GetMapping("/buscar/categoria")
    //    public Page<LivroResponse> buscarPorCategoria(
    //            @RequestParam String categoria,
    //            Pageable pageable) {
    //
    //        return livroService.buscarPorCategoria(categoria, pageable);
    //
    //    }

}