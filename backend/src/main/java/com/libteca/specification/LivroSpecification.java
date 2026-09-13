package com.libteca.specification;

import com.libteca.entity.Livro;
import com.libteca.filter.LivroFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LivroSpecification {

    public static Specification<Livro> comFiltro(LivroFiltro filtro) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            //!Título
            if (filtro.getTitulo() != null &&
                    !filtro.getTitulo().isBlank()) {

                predicates.add(
                        criteriaBuilder.like(
                                root.get("titulo"),
                                "%" + filtro.getTitulo() + "%"
                        )
                );
            }

            //!Autor
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

            //!Categoria
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

            //!Editora
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

            //!Quantia diponível
            if (filtro.getQuantidadeDisponivel() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("quantidadeDisponivel"),
                                filtro.getQuantidadeDisponivel()
                        )
                );
            }

            //!Quantia mínima
            if (filtro.getQuantidadeMinima() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("quantidadeDisponivel"),
                                filtro.getQuantidadeMinima()
                        )
                );
            }

            //!Quantia máxima
            if (filtro.getQuantidadeMaxima() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("quantidadeDisponivel"),
                                filtro.getQuantidadeMaxima()
                        )
                );
            }

            //!Numero de páginas
            if (filtro.getNumeroDePaginas() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("numeroDePaginas"),
                                filtro.getNumeroDePaginas()
                        )
                );
            }

            //!valor mínimo de páginas
            if (filtro.getPaginaMinima() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("numeroDePaginas"),
                                filtro.getPaginaMinima()
                        )
                );
            }

            //!valor máximo de páginas
            if (filtro.getPaginaMaxima() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("numeroDePaginas"),
                                filtro.getPaginaMaxima()
                        )
                );
            }

            //!ano do livro
            if (filtro.getAno() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("ano"),
                                filtro.getAno()
                        )
                );
            }

            //!ano mínimo
            if (filtro.getAnoMinimo() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("ano"),
                                filtro.getAnoMinimo()
                        )
                );
            }

            //!ano máximo
            if (filtro.getAnoMaximo() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("ano"),
                                filtro.getAnoMaximo()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}