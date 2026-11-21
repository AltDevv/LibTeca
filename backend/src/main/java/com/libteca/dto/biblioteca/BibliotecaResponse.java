package com.libteca.dto.biblioteca;

public record BibliotecaResponse(String nome, String site, boolean configurada,
                                 boolean possuiAdministrador, boolean acervoIniciado) {}
