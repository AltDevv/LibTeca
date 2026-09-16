package com.libteca.handler.emprestimo.handler;

import com.libteca.handler.emprestimo.exception.EmprestimoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class EmprestimoNaoEncontradoHandler {

    @ExceptionHandler(EmprestimoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarEmprestimoNaoEncontrado(
            EmprestimoNaoEncontradoException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "erro", "Empréstimo não encontrado",
                        "mensagem", exception.getMessage()
                ));
    }
}