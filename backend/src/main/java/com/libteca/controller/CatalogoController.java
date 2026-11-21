package com.libteca.controller;

import com.libteca.repository.ExemplarRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.libteca.enums.*;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
    private final ExemplarRepository exemplares;
    public CatalogoController(ExemplarRepository exemplares){this.exemplares=exemplares;}
    public record ExemplarPublico(Long id,TypeLivro tipo,StatusExemplar status){}
    @GetMapping("/{livroId}/exemplares")
    @Transactional(readOnly=true)
    public List<ExemplarPublico> exemplares(@PathVariable Long livroId){
        return exemplares.findByLivroId(livroId).stream().map(e -> new ExemplarPublico(e.getId(),e.getTipo(),e.getStatus())).toList();
    }
}
