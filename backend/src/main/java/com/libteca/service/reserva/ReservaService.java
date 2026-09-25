package com.libteca.service;

import com.libteca.dto.reserva.ReservaRequest;
import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.entity.Livro;
import com.libteca.entity.Reserva;
import com.libteca.entity.Usuario;
import com.libteca.enums.TypeLivro;
import com.libteca.handler.livro.exception.LivroNaoEncontradoException;
import com.libteca.handler.reserva.ReservaFoiCanceladaException;
import com.libteca.handler.reserva.ReservaNaoEncontradaException;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.ReservaMapper;
import com.libteca.repository.LivroRepository;
import com.libteca.repository.ReservaRepository;
import com.libteca.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    private final ReservaMapper reservaMapper;
    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper,
                           LivroRepository livroRepository, UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Todos
    public Page<ReservaResponse> listarTodos(Pageable pageable) {

        return reservaRepository
                .findAll(pageable)
                .map(reservaMapper::toResponse);
    }

    // Todos
    public void apagarTodos() {
        reservaRepository.deleteAll();
    }

    // Adicionar
    public ReservaResponse adicionarReserva(ReservaRequest request) {

        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        if(livro.getLimiteReservas() != null){

            //Verificação de quantas reservas já se tem para verificar se é possível mais uma
            long reservasAtivas = reservaRepository.countByLivroIdAndAtivaTrue(livro.getId());

            if (reservasAtivas >= livro.getLimiteReservas()){
                throw new ReservaFoiCanceladaException("O limite de reservas foi atingido");
            }
        }

        Reserva reserva = reservaMapper.toEntity(request, livro, usuario);

        Reserva salvo = reservaRepository.save(reserva);

        return reservaMapper.toResponse(salvo);
    }

    // Buscar por ID
    public ReservaResponse mostrarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException("Reserva não encontrada"));

        return reservaMapper.toResponse(reserva);
    }

    // Cancelar reserva (marca como inativa em vez de apagar)
    public ReservaResponse cancelarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException("Reserva não encontrada"));

        if (!reserva.getAtiva()) {
            throw new ReservaFoiCanceladaException("Esta reserva já está cancelada");
        }

        reserva.setAtiva(false);

        Reserva salvo = reservaRepository.save(reserva);

        return reservaMapper.toResponse(salvo);
    }

    // Apagar por ID
    public void apagarReserva(Long id) {

        if (!reservaRepository.existsById(id)) {
            throw new ReservaNaoEncontradaException("Reserva não encontrada");
        }

        reservaRepository.deleteById(id);
    }

}
