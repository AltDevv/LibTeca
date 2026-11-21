package com.libteca.dto.reserva;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.libteca.dto.exemplar.ExemplarResponse;
import com.libteca.dto.usuario.UsuarioResponse;
import com.libteca.enums.StatusReserva;
public record ReservaResponse(
        Long id,
        ExemplarResponse exemplar,
        UsuarioResponse usuario,
        Long funcionarioId,
        LocalDate dataReserva,
        StatusReserva status
) {}
