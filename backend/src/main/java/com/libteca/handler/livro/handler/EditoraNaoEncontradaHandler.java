package com.libteca.handler.livro.handler;

import com.libteca.handler.livro.exception.EditoraNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class EditoraNaoEncontradaHandler {

    @ExceptionHandler(EditoraNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tratarEditoraNaoEncontrada(
            EditoraNaoEncontradaException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "erro", "Editora não encontrada",
                        "mensagem", exception.getMessage()
                ));
    }
}