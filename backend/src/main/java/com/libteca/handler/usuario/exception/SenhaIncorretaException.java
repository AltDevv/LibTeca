package com.libteca.handler.usuario.exception;

public class SenhaIncorretaException extends RuntimeException {

    public SenhaIncorretaException(String message) {
        super(message);
    }
}