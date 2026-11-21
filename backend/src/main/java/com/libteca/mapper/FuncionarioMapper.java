package com.libteca.mapper;

import com.libteca.dto.funcionario.*;
import com.libteca.entity.*;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioMapper {

    private final UsuarioMapper usuarioMapper;

    public FuncionarioMapper(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    public Funcionario toEntity(FuncionarioRequest request, Usuario usuario) {
        Funcionario funcionario = new Funcionario();
        funcionario.setUsuario(usuario);
        funcionario.setDataAdmissao(request.dataAdmissao());
        funcionario.setAtivo(request.ativo());
        return funcionario;
    }

    public FuncionarioResponse toResponse(Funcionario funcionario) {
        return new FuncionarioResponse(funcionario.getId(), usuarioMapper.toResponse(funcionario.getUsuario()),
                funcionario.getDataAdmissao(), funcionario.getAtivo());
    }
}
