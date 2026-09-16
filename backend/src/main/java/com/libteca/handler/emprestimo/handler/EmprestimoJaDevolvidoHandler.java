package com.libteca.handler.emprestimo.handler;

import com.libteca.handler.emprestimo.exception.EmprestimoJaDevolvidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class EmprestimoJaDevolvidoHandler {

    @ExceptionHandler(EmprestimoJaDevolvidoException.class)
    public ResponseEntity<Map<String, Object>> tratarEmprestimoJaDevolvido(
            EmprestimoJaDevolvidoException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 409,
                        "erro", "Empréstimo já devolvido",
                        "mensagem", exception.getMessage()
                ));
    }
}