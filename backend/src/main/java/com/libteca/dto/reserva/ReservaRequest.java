package com.libteca.dto.reserva;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(

        @NotNull(message = "O livro é obrigatório")
        Long livroId,

        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId

) {}
