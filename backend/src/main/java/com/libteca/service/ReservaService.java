package com.libteca.service;

import com.libteca.dto.reserva.ReservaRequest;
import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.entity.Livro;
import com.libteca.entity.Reserva;
import com.libteca.entity.Usuario;
import com.libteca.mapper.ReservaMapper;
import com.libteca.repository.LivroRepository;
import com.libteca.repository.ReservaRepository;
import com.libteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<ReservaResponse> listarTodos() {
        List<Reserva> reservas = reservaRepository.findAll();

        List<ReservaResponse> resposta = reservas.stream()
                .map(reserva -> reservaMapper.toResponse(reserva))
                .toList();

        return resposta;
    }

    // Todos
    public void apagarTodos() {
        reservaRepository.deleteAll();
    }

    // Adicionar
    public ReservaResponse adicionarReserva(ReservaRequest request) {

        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Reserva reserva = reservaMapper.toEntity(request, livro, usuario);

        Reserva salvo = reservaRepository.save(reserva);

        return reservaMapper.toResponse(salvo);
    }

    // Buscar por ID
    public ReservaResponse mostrarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        return reservaMapper.toResponse(reserva);
    }

    // Cancelar reserva (marca como inativa em vez de apagar)
    public ReservaResponse cancelarReserva(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        if (!reserva.getAtiva()) {
            throw new RuntimeException("Esta reserva já está cancelada");
        }

        reserva.setAtiva(false);

        Reserva salvo = reservaRepository.save(reserva);

        return reservaMapper.toResponse(salvo);
    }

    // Apagar por ID
    public void apagarReserva(Long id) {

        if (!reservaRepository.existsById(id)) {
            throw new RuntimeException("Reserva não encontrada");
        }

        reservaRepository.deleteById(id);
    }

}
