package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.libteca.enums.StatusExemplar;
import com.libteca.enums.TypeLivro;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "exemplar")
public class Exemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idexemplar")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idlivro", nullable = false)
    private Livro livro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status_exemplar", nullable = false, length = 45)
    private StatusExemplar status = StatusExemplar.DISPONIVEL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_exemplar", nullable = false, length = 45)
    private TypeLivro tipo;

    @NotNull
    @Column(name = "data_aquisicao", nullable = false)
    private LocalDate dataAquisicao;

    @Size(max = 255)
    @Column(name = "caminho_arquivo", length = 255)
    private String caminhoArquivo;

    @Size(max = 60)
    @Column(name = "codigo_patrimonio", unique = true, length = 60)
    private String codigoPatrimonio;

}
