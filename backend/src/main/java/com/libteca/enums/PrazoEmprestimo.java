package com.libteca.enums;

import java.time.LocalDate;
public enum PrazoEmprestimo {
    UMA_SEMANA, UM_MES, DOIS_MESES;
    public LocalDate calcular(LocalDate inicio) {
        return switch (this) {
            case UMA_SEMANA -> inicio.plusWeeks(1);
            case UM_MES -> inicio.plusMonths(1);
            case DOIS_MESES -> inicio.plusMonths(2);
        };
    }
}
