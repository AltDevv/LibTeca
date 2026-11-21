package com.libteca.service.reserva;

import com.libteca.entity.*;
import com.libteca.enums.*;
import com.libteca.repository.*;
import com.libteca.service.ExemplarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ReservaLancamentoService {

    public static final List<StatusReserva> EM_ANDAMENTO =
            List.of(StatusReserva.ATIVA, StatusReserva.DISPONIVEL_PARA_RETIRADA);

    private final ReservaRepository reservaRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ExemplarService exemplarService;

    public ReservaLancamentoService(ReservaRepository reservaRepository, EmprestimoRepository emprestimoRepository,
                                    ExemplarService exemplarService) {
        this.reservaRepository = reservaRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.exemplarService = exemplarService;
    }

    // O chamador já bloqueou o exemplar na transação.
    public void atualizarDisponibilidade(Exemplar exemplar) {
        if (exemplar.getTipo() == TypeLivro.VIRTUAL) {
            exemplar.setStatus(exemplar.getCaminhoArquivo() == null || exemplar.getCaminhoArquivo().isBlank()
                    ? StatusExemplar.INDISPONIVEL : StatusExemplar.DISPONIVEL);
            if (!exemplar.getLivro().getDataLancamento().isAfter(LocalDate.now()))
                for (Reserva reserva : reservaRepository.findByExemplarIdAndStatusInOrderByDataReservaAscIdAsc(exemplar.getId(), EM_ANDAMENTO))
                    reserva.setStatus(StatusReserva.DISPONIVEL_PARA_RETIRADA);
            return;
        }
        List<Emprestimo> emprestimos = emprestimoRepository.buscarEmAndamentoPorExemplar(exemplar.getId());
        if (!emprestimos.isEmpty()) {
            exemplar.setStatus(emprestimos.get(0).getStatus() == StatusEmprestimo.PENDENTE
                    ? StatusExemplar.RESERVADO : StatusExemplar.EMPRESTADO);
            return;
        }
        List<Reserva> reservas = reservaRepository.findByExemplarIdAndStatusInOrderByDataReservaAscIdAsc(
                exemplar.getId(), EM_ANDAMENTO);
        if (!reservas.isEmpty()) {
            Reserva reserva = reservas.get(0);
            exemplarService.recarregar(reserva);
            reserva.setStatus(exemplar.getLivro().getDataLancamento().isAfter(LocalDate.now())
                    ? StatusReserva.ATIVA : StatusReserva.DISPONIVEL_PARA_RETIRADA);
            exemplar.setStatus(StatusExemplar.RESERVADO);
        } else {
            exemplar.setStatus(StatusExemplar.DISPONIVEL);
        }
    }

    public void processarReservasDeLancamento() {
        List<Reserva> reservas = reservaRepository.findByStatusAndExemplarLivroDataLancamentoLessThanEqual(
                StatusReserva.ATIVA, LocalDate.now());
        for (Reserva reserva : reservas) {
            Exemplar exemplar = exemplarService.bloquear(reserva.getExemplar().getId());
            atualizarDisponibilidade(exemplar);
        }
    }
}
