package com.libteca.mapper;

import com.libteca.dto.exemplar.*;
import com.libteca.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ExemplarMapper {

    private final LivroMapper livroMapper;
    private final com.libteca.repository.EmprestimoRepository emprestimos;

    public ExemplarMapper(LivroMapper livroMapper, com.libteca.repository.EmprestimoRepository emprestimos) {
        this.emprestimos = emprestimos;
        this.livroMapper = livroMapper;
    }

    public Exemplar toEntity(ExemplarRequest request, Livro livro) {
        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setTipo(request.tipo());
        exemplar.setDataAquisicao(request.dataAquisicao());
        exemplar.setCaminhoArquivo(request.caminhoArquivo());
        exemplar.setCodigoPatrimonio(request.codigoPatrimonio());
        return exemplar;
    }

    public ExemplarResponse toResponse(Exemplar exemplar) {
        return new ExemplarResponse(exemplar.getId(), livroMapper.toResponse(exemplar.getLivro()),
                exemplar.getStatus(), exemplar.getTipo(), exemplar.getDataAquisicao(),
                exemplar.getCaminhoArquivo(), exemplar.getCodigoPatrimonio(),
                emprestimos.countByExemplarIdAndStatusAndDataDevolucaoIsNullAndDataExpiracaoGreaterThanEqual(exemplar.getId(), com.libteca.enums.StatusEmprestimo.ATIVO, java.time.LocalDate.now()),
                emprestimos.countByExemplarIdAndStatusNot(exemplar.getId(), com.libteca.enums.StatusEmprestimo.CANCELADO));
    }
}
