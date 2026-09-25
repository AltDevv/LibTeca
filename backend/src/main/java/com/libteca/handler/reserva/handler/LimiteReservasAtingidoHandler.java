package com.libteca.handler.reserva.handler;

import com.libteca.handler.reserva.exception.LimiteReservasAtingidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class LimiteReservasAtingidoHandler {

    @ExceptionHandler(LimiteReservasAtingidoException.class)
    public ResponseEntity<Map<String, Object>> tratarLimiteReservasAtingido(
            LimiteReservasAtingidoException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 409,
                        "erro", "Limite de reservas atingido",
                        "mensagem", exception.getMessage()
                ));
    }
}
