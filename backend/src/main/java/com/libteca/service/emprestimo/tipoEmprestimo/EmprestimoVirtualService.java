package com.libteca.service.emprestimo.tipoEmprestimo;

import com.libteca.entity.Livro;

import com.libteca.entity.Livro;
import com.libteca.handler.livro.exception.LivroIndisponivelException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmprestimoVirtualService {
    public LocalDate emprestarVirtual(Livro livro){

        Integer quantidade = livro.getQuantidadeAcessosDisponiveis();

        //O valor nulo é um  livro que pode ser pego de modo infinito
        if (quantidade != null) {

            if (quantidade <= 0) {
                throw new LivroIndisponivelException(
                        "Não há acessos disponíveis para este livro virtual"
                );
            }

            livro.setQuantidadeAcessosDisponiveis(quantidade - 1);
        }
        if (Boolean.TRUE.equals(livro.getPossuiExpiracao())) {

            return LocalDate.now()
                    .plusDays(livro.getDiasExpiracao());
        }

        return null;
    }


}
