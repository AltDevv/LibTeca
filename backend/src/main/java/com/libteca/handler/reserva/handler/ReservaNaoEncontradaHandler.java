package com.libteca.handler.reserva.handler;

import com.libteca.handler.reserva.exception.ReservaNaoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class ReservaNaoEncontradaHandler {

    @ExceptionHandler(ReservaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tratarReservaNaoEncontrada(
            ReservaNaoEncontradaException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "erro", "Reserva não encontrada",
                        "mensagem", exception.getMessage()
                ));
    }
}