package com.libteca.specification;

import com.libteca.entity.*;
import com.libteca.enums.StatusExemplar;
import com.libteca.filter.LivroFiltro;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LivroSpecification {

    public static Specification<Livro> comFiltro(LivroFiltro filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (temTexto(filtro.getTitulo())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), termo(filtro.getTitulo())));
            }
            if (temTexto(filtro.getAutor())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("autores").get("nome")), termo(filtro.getAutor())));
            }
            if (temTexto(filtro.getCategoria())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("categorias").get("nome")), termo(filtro.getCategoria())));
            }
            if (temTexto(filtro.getEditora())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("editora").get("nome")), termo(filtro.getEditora())));
            }
            if (filtro.getDataLancamento() != null) {
                predicates.add(criteriaBuilder.equal(root.get("dataLancamento"), filtro.getDataLancamento()));
            }
            if (filtro.getPossuiPrelancamento() != null) {
                predicates.add(filtro.getPossuiPrelancamento()
                        ? criteriaBuilder.greaterThan(root.get("dataLancamento"), LocalDate.now())
                        : criteriaBuilder.lessThanOrEqualTo(root.get("dataLancamento"), LocalDate.now()));
            }
            if (filtro.getNumeroDePaginas() != null) predicates.add(criteriaBuilder.equal(root.get("numeroDePaginas"), filtro.getNumeroDePaginas()));
            if (filtro.getPaginaMinima() != null) predicates.add(criteriaBuilder.ge(root.get("numeroDePaginas"), filtro.getPaginaMinima()));
            if (filtro.getPaginaMaxima() != null) predicates.add(criteriaBuilder.le(root.get("numeroDePaginas"), filtro.getPaginaMaxima()));

            if (filtro.getAno() != null) predicates.add(criteriaBuilder.between(root.get("dataLancamento"),
                    LocalDate.of(filtro.getAno(), 1, 1), LocalDate.of(filtro.getAno(), 12, 31)));
            if (filtro.getAnoMinimo() != null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataLancamento"), LocalDate.of(filtro.getAnoMinimo(), 1, 1)));
            if (filtro.getAnoMaximo() != null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataLancamento"), LocalDate.of(filtro.getAnoMaximo(), 12, 31)));

            if (filtro.getTipoLivro() != null) {
                Subquery<Long> tipos = query.subquery(Long.class);
                Root<Exemplar> exemplar = tipos.from(Exemplar.class);
                tipos.select(exemplar.get("id")).where(
                        criteriaBuilder.equal(exemplar.get("livro"), root),
                        criteriaBuilder.equal(exemplar.get("tipo"), filtro.getTipoLivro()));
                predicates.add(criteriaBuilder.exists(tipos));
            }
            if (filtro.getQuantidadeDisponivel() != null || filtro.getQuantidadeMinima() != null || filtro.getQuantidadeMaxima() != null) {
                Subquery<Long> quantidade = query.subquery(Long.class);
                Root<Exemplar> exemplar = quantidade.from(Exemplar.class);
                List<Predicate> condicoes = new ArrayList<>();
                condicoes.add(criteriaBuilder.equal(exemplar.get("livro"), root));
                condicoes.add(criteriaBuilder.equal(exemplar.get("status"), StatusExemplar.DISPONIVEL));
                if (filtro.getTipoLivro() != null) condicoes.add(criteriaBuilder.equal(exemplar.get("tipo"), filtro.getTipoLivro()));
                quantidade.select(criteriaBuilder.count(exemplar)).where(condicoes.toArray(new Predicate[0]));
                if (filtro.getQuantidadeDisponivel() != null) predicates.add(criteriaBuilder.equal(quantidade, filtro.getQuantidadeDisponivel().longValue()));
                if (filtro.getQuantidadeMinima() != null) predicates.add(criteriaBuilder.ge(quantidade, filtro.getQuantidadeMinima()));
                if (filtro.getQuantidadeMaxima() != null) predicates.add(criteriaBuilder.le(quantidade, filtro.getQuantidadeMaxima()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    private static String termo(String texto) {
        return "%" + texto.toLowerCase(Locale.ROOT) + "%";
    }
}
