package com.libteca.service.reserva;

import com.libteca.dto.reserva.ReservaRequest;
import com.libteca.entity.Emprestimo;
import com.libteca.entity.Livro;
import com.libteca.entity.Reserva;
import com.libteca.entity.Usuario;
import com.libteca.enums.StatusReserva;
import com.libteca.enums.TypeLivro;
import com.libteca.handler.livro.exception.LivroNaoEncontradoException;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.EmprestimoMapper;
import com.libteca.repository.EmprestimoRepository;
import com.libteca.repository.LivroRepository;
import com.libteca.repository.ReservaRepository;
import com.libteca.repository.UsuarioRepository;
import com.libteca.service.emprestimo.tipoEmprestimo.EmprestimoFisicoService;
import com.libteca.service.emprestimo.tipoEmprestimo.EmprestimoVirtualService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class ReservaLancamentoService {

    private final LivroRepository livroRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoVirtualService emprestimoVirtualService;
    private final EmprestimoFisicoService emprestimoFisicoService;
    private final EmprestimoMapper emprestimoMapper;
    private final EmprestimoRepository emprestimoRepository;

    public ReservaLancamentoService(LivroRepository livroRepository, ReservaRepository reservaRepository, UsuarioRepository
                                    usuarioRepository, EmprestimoVirtualService emprestimoVirtualService,
                                    EmprestimoFisicoService emprestimoFisicoService,
                                    EmprestimoMapper emprestimoMapper, EmprestimoRepository emprestimoRepository) {

        this.livroRepository = livroRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoVirtualService = emprestimoVirtualService;
        this.emprestimoFisicoService = emprestimoFisicoService;
        this.emprestimoMapper = emprestimoMapper;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional
    public void processarReservasDeLancamento() {

        LocalDate hoje = LocalDate.now();

        //alguns exemplos:
        //
        //Data do lançamento	Hoje	Comparação	Entra na busca?	O que acontece
           //20/09/2026	    19/09/2026	    20 <= 19	  Não	    Continua ATIVA
           //19/09/2026	    19/09/2026	    19 <= 19	  Sim	    Vira DISPONIVEL_PARA_RETIRADA
           //18/09/2026	    19/09/2026	    18 <= 19	  Sim	    Vira DISPONIVEL_PARA_RETIRADA
           //10/09/2026	    19/09/2026	    10 <= 19	  Sim	    Vira DISPONIVEL_PARA_RETIRADA, se ainda estiver ATIVA
        List<Reserva> reservas =
                reservaRepository
                        .findByStatusAndLivroPossuiPrelancamentoTrueAndLivroDataLancamentoLessThanEqual(
                                StatusReserva.ATIVA,
                                hoje
                        );

        for (Reserva reserva : reservas) {

            reserva.setStatus(
                    StatusReserva.DISPONIVEL_PARA_RETIRADA
            );

            reservaRepository.save(reserva);
        }
    }
}