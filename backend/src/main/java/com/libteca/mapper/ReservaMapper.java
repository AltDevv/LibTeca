package com.libteca.mapper;

import com.libteca.dto.reserva.ReservaRequest;
import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.entity.Livro;
import com.libteca.entity.Reserva;
import com.libteca.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReservaMapper {

    private final LivroMapper livroMapper;
    private final UsuarioMapper usuarioMapper;

    public ReservaMapper(LivroMapper livroMapper, UsuarioMapper usuarioMapper) {
        this.livroMapper = livroMapper;
        this.usuarioMapper = usuarioMapper;
    }

    public Reserva toEntity(ReservaRequest request, Livro livro, Usuario usuario) {

        Reserva reserva = new Reserva();

        reserva.setLivro(livro);
        reserva.setUsuario(usuario);
        reserva.setDataReserva(LocalDate.now());
        reserva.setAtiva(true);

        return reserva;
    }

    public ReservaResponse toResponse(Reserva reserva) {

        return new ReservaResponse(
                reserva.getId(),
                livroMapper.toResponse(reserva.getLivro()),
                usuarioMapper.toResponse(reserva.getUsuario()),
                reserva.getDataReserva(),
                reserva.getAtiva()
        );

    }

}
