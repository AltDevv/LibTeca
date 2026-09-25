package com.libteca.dto.reserva;

import com.libteca.dto.livro.LivroResponse;
import com.libteca.dto.usuario.UsuarioResponse;

import java.time.LocalDate;

public record ReservaResponse(

        Long id,
        LivroResponse livro,
        UsuarioResponse usuario,
        LocalDate dataReserva

) {}
