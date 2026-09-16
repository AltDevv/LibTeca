package com.libteca.specification;

import com.libteca.entity.Livro;
import com.libteca.enums.TypeLivro;
import com.libteca.filter.LivroFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LivroSpecification {

    public static Specification<Livro> comFiltro(LivroFiltro filtro) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            //! TEXTO
            // Título
            if (filtro.getTitulo() != null &&
                    !filtro.getTitulo().isBlank()) {

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("titulo")
                                ),
                                "%" + filtro.getTitulo().toLowerCase() + "%"
                        )
                );
            }

            // Autor
            if (filtro.getAutor() != null &&
                    !filtro.getAutor().isBlank()) {

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.join("autor").get("nome")
                                ),
                                "%" + filtro.getAutor().toLowerCase() + "%"
                        )
                );
            }

            // Categoria
            if (filtro.getCategoria() != null &&
                    !filtro.getCategoria().isBlank()) {

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.join("categoria").get("nome")
                                ),
                                "%" + filtro.getCategoria().toLowerCase() + "%"
                        )
                );
            }

            // Editora
            if (filtro.getEditora() != null &&
                    !filtro.getEditora().isBlank()) {

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.join("editora").get("nome")
                                ),
                                "%" + filtro.getEditora().toLowerCase() + "%"
                        )
                );
            }

            //! TIPO
            if (filtro.getTipoLivro() != null &&
                    !filtro.getTipoLivro().isBlank()) {

                try {
                    TypeLivro tipo = TypeLivro.valueOf(
                            filtro.getTipoLivro().toUpperCase()
                    );

                    predicates.add(
                            criteriaBuilder.equal(
                                    root.get("tipo"),
                                    tipo
                            )
                    );

                } catch (IllegalArgumentException exception) {
                    // Tipo inválido: não adiciona filtro.
                }
            }

            //! VIRTUAL
            // Permite download
            if (filtro.getPermiteDownload() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("permiteDownload"),
                                filtro.getPermiteDownload()
                        )
                );
            }

            //Se for true, o usuário quer livros com acesso ilimitado
            //Se false ele que livros com acesso finito
            //Null, ele não quer filtrar por isso
            if (filtro.getAcessosIlimitados() != null) {

                if (filtro.getAcessosIlimitados()) {

                    //true vira Valor infinito
                    predicates.add(
                            criteriaBuilder.isNull(
                                    root.get("quantidadeAcessosDisponiveis")
                            )
                    );

                } else {

                    //false vira Valor finito
                    predicates.add(
                            criteriaBuilder.isNotNull(
                                    root.get("quantidadeAcessosDisponiveis")
                            )
                    );
                }
            }

            // Quantidade exata de acessos
            if (filtro.getQuantidadeAcessosDisponiveis() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.<Integer>get(
                                        "quantidadeAcessosDisponiveis"
                                ),
                                filtro.getQuantidadeAcessosDisponiveis()
                        )
                );
            }

            // Mínimo de acessos
            if (filtro.getAcessosMinimos() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get(
                                        "quantidadeAcessosDisponiveis"
                                ),
                                filtro.getAcessosMinimos()
                        )
                );
            }

            // Máximo de acessos
            if (filtro.getAcessosMaximos() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get(
                                        "quantidadeAcessosDisponiveis"
                                ),
                                filtro.getAcessosMaximos()
                        )
                );
            }

            // Possui expiração
            if (filtro.getPossuiExpiracao() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("possuiExpiracao"),
                                filtro.getPossuiExpiracao()
                        )
                );
            }

            // Dias de expiração
            if (filtro.getDiasExpiracao() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("diasExpiracao"),
                                filtro.getDiasExpiracao()
                        )
                );
            }

            //! LANÇAMENTO
            // Possui pré-lançamento
            if (filtro.getPossuiPrelancamento() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("possuiPrelancamento"),
                                filtro.getPossuiPrelancamento()
                        )
                );
            }

            // Data de lançamento
            if (filtro.getDataLancamento() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("dataLancamento"),
                                filtro.getDataLancamento()
                        )
                );
            }

            //! FÍSICO
            // Quantidade disponível exata
            if (filtro.getQuantidadeDisponivel() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.<Integer>get(
                                        "quantidadeDisponivel"
                                ),
                                filtro.getQuantidadeDisponivel()
                        )
                );
            }

            // Quantidade mínima
            if (filtro.getQuantidadeMinima() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get(
                                        "quantidadeDisponivel"
                                ),
                                filtro.getQuantidadeMinima()
                        )
                );
            }

            // Quantidade máxima
            if (filtro.getQuantidadeMaxima() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get(
                                        "quantidadeDisponivel"
                                ),
                                filtro.getQuantidadeMaxima()
                        )
                );
            }

            //! PÁGINAS
            // Número exato de páginas
            if (filtro.getNumeroDePaginas() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.<Integer>get(
                                        "numeroDePaginas"
                                ),
                                filtro.getNumeroDePaginas()
                        )
                );
            }

            // Mínimo de páginas
            if (filtro.getPaginaMinima() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get(
                                        "numeroDePaginas"
                                ),
                                filtro.getPaginaMinima()
                        )
                );
            }

            // Máximo de páginas
            if (filtro.getPaginaMaxima() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get(
                                        "numeroDePaginas"
                                ),
                                filtro.getPaginaMaxima()
                        )
                );
            }

            //! ANO
            // Ano exato
            if (filtro.getAno() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.<Integer>get("ano"),
                                filtro.getAno()
                        )
                );
            }

            // Ano mínimo
            if (filtro.getAnoMinimo() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<Integer>get("ano"),
                                filtro.getAnoMinimo()
                        )
                );
            }

            // Ano máximo
            if (filtro.getAnoMaximo() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<Integer>get("ano"),
                                filtro.getAnoMaximo()
                        )
                );
            }

            //! Junta todos os filtros com AND
            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}