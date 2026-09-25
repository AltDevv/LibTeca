package com.libteca.handler.reserva.handler;

import com.libteca.handler.reserva.exception.ReservaFoiCanceladaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class ReservaFoiCanceladaHandler {

    @ExceptionHandler(ReservaFoiCanceladaException.class)
    public ResponseEntity<Map<String, Object>> tratarReservaFoiCancelada(
            ReservaFoiCanceladaException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 409,
                        "erro", "Reserva já cancelada",
                        "mensagem", exception.getMessage()
                ));
    }
}