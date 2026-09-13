package com.libteca.handler.usuario.exception;

public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException(String message) {
        super(message);
    }
}