package com.libteca.mapper;

import com.libteca.dto.emprestimo.EmprestimoRequest;
import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.entity.Emprestimo;
import com.libteca.entity.Livro;
import com.libteca.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmprestimoMapper {

    private final LivroMapper livroMapper;
    private final UsuarioMapper usuarioMapper;

    public EmprestimoMapper(
            LivroMapper livroMapper,
            UsuarioMapper usuarioMapper
    ) {
        this.livroMapper = livroMapper;
        this.usuarioMapper = usuarioMapper;
    }

    public Emprestimo toEntity(
            Livro livro,
            Usuario usuario,
            LocalDate dataEmprestimo,
            LocalDate dataExpiracao
    ) {

        Emprestimo emprestimo = new Emprestimo();

        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataExpiracao(dataExpiracao);

        return emprestimo;
    }

    public EmprestimoResponse toResponse(Emprestimo emprestimo) {

        return new EmprestimoResponse(
                emprestimo.getId(),
                livroMapper.toResponse(emprestimo.getLivro()),
                usuarioMapper.toResponse(emprestimo.getUsuario()),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataExpiracao(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus()
        );
    }
}
