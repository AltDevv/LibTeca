package com.libteca.mapper;

import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.entity.*;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    private final ExemplarMapper exemplarMapper;
    private final UsuarioMapper usuarioMapper;

    public ReservaMapper(ExemplarMapper exemplarMapper, UsuarioMapper usuarioMapper) {
        this.exemplarMapper = exemplarMapper;
        this.usuarioMapper = usuarioMapper;
    }

    public Reserva toEntity(Exemplar exemplar, Usuario usuario, Funcionario funcionario) {
        Reserva reserva = new Reserva();
        reserva.setExemplar(exemplar);
        reserva.setUsuario(usuario);
        reserva.setFuncionario(funcionario);
        reserva.setDataReserva(LocalDate.now());
        return reserva;
    }

    public ReservaResponse toResponse(Reserva reserva) {
        return new ReservaResponse(reserva.getId(), exemplarMapper.toResponse(reserva.getExemplar()),
                usuarioMapper.toResponse(reserva.getUsuario()),
                reserva.getFuncionario() == null ? null : reserva.getFuncionario().getId(),
                reserva.getDataReserva(), reserva.getStatus());
    }
}
