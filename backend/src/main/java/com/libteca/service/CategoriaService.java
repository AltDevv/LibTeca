package com.libteca.service;

import com.libteca.handler.livro.exception.CategoriaNaoEncontradaException;

import com.libteca.dto.categoria.CategoriaRequest;
import com.libteca.dto.categoria.CategoriaResponse;
import com.libteca.entity.Categoria;
import com.libteca.mapper.CategoriaMapper;
import com.libteca.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    // Todos
    public List<CategoriaResponse> listarTodos() {
        List<Categoria> categorias = categoriaRepository.findAll();

        List<CategoriaResponse> resposta = categorias.stream()
                .map(categoria -> categoriaMapper.toResponse(categoria))
                .toList();

        return resposta;
    }

    // Todos
    public void apagarTodos() {
        categoriaRepository.deleteAll();
    }

    // Adicionar
    public CategoriaResponse adicionarCategoria(CategoriaRequest request) {

        Categoria categoria = categoriaMapper.toEntity(request);

        Categoria salvo = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(salvo);
    }

    // Buscar por ID
    public CategoriaResponse mostrarCategoria(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException("Categoria não encontrada"));

        return categoriaMapper.toResponse(categoria);
    }

    // Apagar por ID
    public void apagarCategoria(Long id) {

        if (!categoriaRepository.existsById(id)) {
            throw new CategoriaNaoEncontradaException("Categoria não encontrada");
        }

        categoriaRepository.deleteById(id);
    }

}
