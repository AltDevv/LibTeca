package com.libteca.service.reserva;

import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.dto.reserva.ReservaRequest;
import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.entity.Emprestimo;
import com.libteca.entity.Livro;
import com.libteca.entity.Reserva;
import com.libteca.entity.Usuario;
import com.libteca.enums.StatusReserva;
import com.libteca.enums.TypeLivro;
import com.libteca.handler.livro.exception.LivroNaoEncontradoException;
import com.libteca.handler.reserva.exception.*;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.EmprestimoMapper;
import com.libteca.mapper.ReservaMapper;
import com.libteca.repository.EmprestimoRepository;
import com.libteca.repository.LivroRepository;
import com.libteca.repository.ReservaRepository;
import com.libteca.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.libteca.service.emprestimo.tipoEmprestimo.EmprestimoFisicoService;
import com.libteca.service.emprestimo.tipoEmprestimo.EmprestimoVirtualService;


import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaMapper reservaMapper;
    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoMapper emprestimoMapper;
    private final EmprestimoVirtualService emprestimoVirtualService;
    private final EmprestimoFisicoService emprestimoFisicoService;
    private final EmprestimoRepository emprestimoRepository;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper,
                          LivroRepository livroRepository, UsuarioRepository usuarioRepository,
                          EmprestimoMapper emprestimoMapper, EmprestimoVirtualService emprestimoVirtualService,
                          EmprestimoFisicoService emprestimoFisicoService, EmprestimoRepository emprestimoRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoMapper = emprestimoMapper;
        this.emprestimoFisicoService = emprestimoFisicoService;
        this.emprestimoVirtualService = emprestimoVirtualService;
        this.emprestimoRepository = emprestimoRepository;
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

        //
        List<StatusReserva> statusEmAndamento = List.of(StatusReserva.ATIVA, StatusReserva.DISPONIVEL_PARA_RETIRADA);

        boolean jaExisteReserva =
                reservaRepository.existsByLivroIdAndUsuarioIdAndStatusIn(
                        livro.getId(),
                        usuario.getId(),
                        statusEmAndamento
                );


        if (jaExisteReserva) {
            throw new JaPossuiReservaException(
                    "Este usuário já possui uma reserva ativa para este livro"
            );
        }

        if(livro.getLimiteReservas() != null){

            //Verificação de quantas reservas já se tem para verificar se é possível mais uma
            long reservasAtivas = reservaRepository.countByLivroIdAndStatusIn(livro.getId(), statusEmAndamento);


            if (reservasAtivas >= livro.getLimiteReservas()){
                throw new LimiteReservasAtingidoException("O limite de reservas foi atingido");
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

        if (reserva.getStatus().equals(StatusReserva.CANCELADA)) {
            throw new ReservaFoiCanceladaException("Esta reserva já está cancelada");
        }

        reserva.setStatus(StatusReserva.CANCELADA);

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

    @Transactional
    public EmprestimoResponse retirarReserva(Long id){

        //Verificar se a reserva existe antes de retirar ela
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() ->
                        new ReservaNaoEncontradaException("Reserva não encontrada")
                );

        if (reserva.getStatus() != StatusReserva.DISPONIVEL_PARA_RETIRADA) {
            throw new ReservaNaoDisponivelParaRetiradaException(
                    "Esta reserva não está disponível para retirada"
            );
        }

        //Buscando da reserva suas informações
        Livro livro = reserva.getLivro();
        Usuario usuario = reserva.getUsuario();

        //Data de emprestimo e possível expiração
        LocalDate dataDeRetirada = LocalDate.now();
        LocalDate dataExpiracao = null;


        if (livro.getTipo() == TypeLivro.FISICO) {

            emprestimoFisicoService.emprestarFisico(livro);

        } else if (livro.getTipo() == TypeLivro.VIRTUAL) {

            dataExpiracao =
                    emprestimoVirtualService.emprestarVirtual(livro);
        }

        Emprestimo emprestimo =
                emprestimoMapper.toEntity(
                        livro,
                        usuario,
                        dataDeRetirada,
                        dataExpiracao
                );

        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        reserva.setStatus(StatusReserva.CONCLUIDA);

        return emprestimoMapper.toResponse(salvo);
    }
}
