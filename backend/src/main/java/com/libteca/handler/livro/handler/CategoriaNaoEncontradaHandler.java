package com.libteca.handler.livro.handler;

import com.libteca.handler.livro.exception.CategoriaNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class CategoriaNaoEncontradaHandler {

    @ExceptionHandler(CategoriaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tratarCategoriaNaoEncontrada(
            CategoriaNaoEncontradaException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "erro", "Categoria não encontrada",
                        "mensagem", exception.getMessage()
                ));
    }
}