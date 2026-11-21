package com.libteca.controller;

import com.libteca.service.*;
import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.dto.usuario.SenhaRequest;
import com.libteca.dto.telefone.*;
import com.libteca.repository.TelefoneRepository;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/minha-conta")
public class MinhaContaController {
    private final MinhaContaService conta;
    private final AcessoService acesso;
    private final UsuarioService usuarios;
    private final TelefoneService telefones;
    private final TelefoneRepository telefoneRepository;
    public MinhaContaController(MinhaContaService conta,AcessoService acesso,UsuarioService usuarios,
            TelefoneService telefones,TelefoneRepository telefoneRepository) {
        this.conta=conta;this.acesso=acesso;this.usuarios=usuarios;this.telefones=telefones;this.telefoneRepository=telefoneRepository;
    }
    @GetMapping("/emprestimos") public List<EmprestimoResponse> emprestimos(){ return conta.emprestimos(); }
    @GetMapping("/reservas") public List<ReservaResponse> reservas(){ return conta.reservas(); }
    @PostMapping("/reservas/{exemplarId}") public ReservaResponse reservar(@PathVariable Long exemplarId){ return conta.reservar(exemplarId); }
    @PostMapping("/emprestimos/{exemplarId}") public EmprestimoResponse emprestar(@PathVariable Long exemplarId, @Valid @RequestBody com.libteca.dto.emprestimo.PrazoRequest request){ return conta.emprestar(exemplarId,request.prazo()); }
    @PatchMapping("/emprestimos/{id}/devolver") public EmprestimoResponse devolver(@PathVariable Long id){ return conta.devolver(id); }
    @PatchMapping("/emprestimos/{id}/receber") public EmprestimoResponse receber(@PathVariable Long id){ return conta.confirmarRecebimento(id); }
    @PatchMapping("/emprestimos/{id}/cancelar") public EmprestimoResponse cancelarEmprestimo(@PathVariable Long id){ return conta.cancelarEmprestimo(id); }
    @PatchMapping("/reservas/{id}/cancelar") public ReservaResponse cancelar(@PathVariable Long id){ return conta.cancelar(id); }
    @PatchMapping("/reservas/{id}/retirar") public EmprestimoResponse retirar(@PathVariable Long id, @Valid @RequestBody com.libteca.dto.emprestimo.PrazoRequest request){ return conta.retirar(id,request.prazo()); }
    @PatchMapping("/senha") public void senha(@Valid @RequestBody SenhaRequest request){ usuarios.alterarSenha(acesso.atual().getId(),request); }
    @GetMapping("/telefones") public List<TelefoneResponse> telefones(){ return telefones.buscarPorUsuario(acesso.atual().getId()); }
    @PostMapping("/telefones") public TelefoneResponse telefone(@Valid @RequestBody TelefoneRequest request){
        return telefones.adicionarTelefone(new TelefoneRequest(acesso.atual().getId(),request.numero()));
    }
    @DeleteMapping("/telefones/{id}") public void apagarTelefone(@PathVariable Long id){
        var telefone=telefoneRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(telefone.getUsuario().getId()); telefones.apagarTelefone(id);
    }
}
