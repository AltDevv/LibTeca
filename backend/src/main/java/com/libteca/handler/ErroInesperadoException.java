package com.libteca.handler;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class ErroInesperadoException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarErroDeValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        erro -> erro.getField(),
                        erro -> erro.getDefaultMessage(),
                        (error1, error2) -> error1 + "; " + error2
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "erro", "Erro de validação",
                        "campos", campos
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> tratarRegra(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of(
                "status", exception.getStatusCode().value(), "mensagem",
                exception.getReason() == null ? "Operação não permitida" : exception.getReason()));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class})
    public ResponseEntity<Map<String, Object>> tratarDadosInvalidos(Exception exception) {
        String mensagem = exception instanceof IllegalArgumentException
                ? exception.getMessage() : "Verifique os campos, as datas e os valores informados";
        return ResponseEntity.badRequest().body(Map.of("status", 400, "mensagem", mensagem == null ? "Dados inválidos" : mensagem));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> tratarIntegridade(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409, "mensagem", "Registro duplicado ou vinculado a outros dados. Verifique os relacionamentos"));
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, Object>> tratarConcorrencia(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409, "mensagem", "Outra operação alterou este registro. Atualize os dados e tente novamente"));
    }

}
