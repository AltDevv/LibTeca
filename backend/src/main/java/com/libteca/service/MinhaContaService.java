package com.libteca.service;

import com.libteca.dto.emprestimo.*;
import com.libteca.dto.reserva.*;
import com.libteca.enums.TypeLivro;
import com.libteca.mapper.*;
import com.libteca.repository.*;
import com.libteca.service.emprestimo.EmprestimoService;
import com.libteca.service.reserva.ReservaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MinhaContaService {
    private final AcessoService acesso;
    private final EmprestimoRepository emprestimos;
    private final ReservaRepository reservas;
    private final ExemplarRepository exemplares;
    private final EmprestimoMapper emprestimoMapper;
    private final ReservaMapper reservaMapper;
    private final EmprestimoService emprestimoService;
    private final ReservaService reservaService;
    public MinhaContaService(AcessoService acesso, EmprestimoRepository emprestimos, ReservaRepository reservas,
            ExemplarRepository exemplares, EmprestimoMapper emprestimoMapper, ReservaMapper reservaMapper,
            EmprestimoService emprestimoService, ReservaService reservaService) {
        this.acesso=acesso; this.emprestimos=emprestimos; this.reservas=reservas; this.exemplares=exemplares;
        this.emprestimoMapper=emprestimoMapper; this.reservaMapper=reservaMapper;
        this.emprestimoService=emprestimoService; this.reservaService=reservaService;
    }
    public List<EmprestimoResponse> emprestimos() {
        return emprestimos.findByUsuarioIdOrderByIdDesc(acesso.atual().getId()).stream().map(emprestimoMapper::toResponse).toList();
    }
    public List<ReservaResponse> reservas() {
        return reservas.findByUsuarioIdOrderByIdDesc(acesso.atual().getId()).stream().map(reservaMapper::toResponse).toList();
    }
    public ReservaResponse reservar(Long exemplarId) {
        return reservaService.adicionarReserva(new ReservaRequest(exemplarId, acesso.atual().getId(), null));
    }
    public EmprestimoResponse emprestar(Long exemplarId, com.libteca.enums.PrazoEmprestimo prazo) {
        var exemplar=exemplares.findById(exemplarId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var pedido=new EmprestimoRequest(exemplarId,acesso.atual().getId(),null,prazo.calcular(LocalDate.now()),prazo);
        return exemplar.getTipo()==TypeLivro.VIRTUAL?emprestimoService.adicionarEmprestimoDigital(pedido)
                :emprestimoService.solicitarEmprestimoFisico(pedido);
    }
    public EmprestimoResponse confirmarRecebimento(Long id) {
        var emprestimo=emprestimos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(emprestimo.getUsuario().getId());
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A entrega deve ser confirmada por um funcionário ou administrador");
    }
    public EmprestimoResponse cancelarEmprestimo(Long id) {
        var emprestimo=emprestimos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(emprestimo.getUsuario().getId());
        return emprestimoService.cancelarSolicitacao(id);
    }
    public EmprestimoResponse devolver(Long id) {
        var emprestimo=emprestimos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(emprestimo.getUsuario().getId());
        if(emprestimo.getExemplar().getTipo()!=TypeLivro.VIRTUAL) throw new ResponseStatusException(HttpStatus.CONFLICT,"Entregue o exemplar físico a um funcionário");
        return emprestimoService.devolverEmprestimo(id);
    }
    public ReservaResponse cancelar(Long id) {
        var reserva=reservas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(reserva.getUsuario().getId());
        return reservaService.cancelarReserva(id);
    }
    public EmprestimoResponse retirar(Long id, com.libteca.enums.PrazoEmprestimo prazo) {
        var reserva=reservas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(reserva.getUsuario().getId());
        if (reserva.getExemplar().getTipo() == TypeLivro.FISICO)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A entrega deve ser confirmada por um funcionário ou administrador");
        return reservaService.retirarReserva(id,new RetiradaReservaRequest(prazo.calcular(LocalDate.now()),null,prazo));
    }
}
