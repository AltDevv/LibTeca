package com.libteca.enums;

public enum Role {
    USER,
    FUNCIONARIO,
    ADMIN,
    // Compatibilidade com os cadastros que usavam o nome antigo.
    @Deprecated
    ADMIM
}
