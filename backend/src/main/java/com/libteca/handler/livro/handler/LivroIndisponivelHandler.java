package com.libteca.handler.livro.handler;

import com.libteca.handler.livro.exception.LivroIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class LivroIndisponivelHandler {

    @ExceptionHandler(LivroIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> tratarLivroIndisponivel(
            LivroIndisponivelException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 409,
                        "erro", "Livro indisponível",
                        "mensagem", exception.getMessage()
                ));
    }
}