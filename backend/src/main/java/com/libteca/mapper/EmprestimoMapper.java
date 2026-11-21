package com.libteca.mapper;

import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.entity.*;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class EmprestimoMapper {

    private final ExemplarMapper exemplarMapper;
    private final UsuarioMapper usuarioMapper;

    public EmprestimoMapper(ExemplarMapper exemplarMapper, UsuarioMapper usuarioMapper) {
        this.exemplarMapper = exemplarMapper;
        this.usuarioMapper = usuarioMapper;
    }

    public Emprestimo toEntity(Exemplar exemplar, Usuario usuario, Funcionario funcionario, LocalDate dataPrevistaDevolucao) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setExemplar(exemplar);
        emprestimo.setUsuario(usuario);
        emprestimo.setFuncionario(funcionario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataExpiracao(dataPrevistaDevolucao);
        return emprestimo;
    }

    public EmprestimoResponse toResponse(Emprestimo emprestimo) {
        return new EmprestimoResponse(emprestimo.getId(), exemplarMapper.toResponse(emprestimo.getExemplar()),
                usuarioMapper.toResponse(emprestimo.getUsuario()),
                emprestimo.getFuncionario() == null ? null : emprestimo.getFuncionario().getId(),
                emprestimo.getDataEmprestimo(), emprestimo.getDataExpiracao(), emprestimo.getDataDevolucao(), emprestimo.getStatus(), emprestimo.getPrazo());
    }
}
