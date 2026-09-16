package com.libteca.service.emprestimo.tipoEmprestimo;

import com.libteca.entity.Livro;
import com.libteca.handler.livro.exception.LivroIndisponivelException;
import org.springframework.stereotype.Service;

@Service
public class EmprestimoFisicoService {

    public void emprestarFisico(Livro livro){
        if (livro.getQuantidadeDisponivel() <= 0) {
            throw new LivroIndisponivelException("Não há exemplares disponíveis para empréstimo");
        }
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() - 1);


    }

    public void devolverFisico(Livro livro){
            livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() + 1);
    }
}
