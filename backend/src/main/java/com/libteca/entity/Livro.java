package com.libteca.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity

public class Livro{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    //Id
    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id=id;
    }

    //título
    @Column(nullable = false)
    private String titulo;

    public String getTitulo(){
        return titulo;
    }

    public void setTitulo(String titulo){
        this.titulo = titulo;
    }

    //autor
    @Column(nullable = false)
    private String autor;

    public String getAutor(){
        return autor;
    }

    public void setAutor(String autor){
        this.autor=autor;
    }

    @Column(nullable = false)
    private String categoria;

    public String getCategoria(){
        return categoria;
    }

    public void setCategoria(String categoria){
        this.categoria=categoria;
    }




    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    public Integer getQuantidadeDisponivel(){
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(Integer quantidadeDisponivel){
        this.quantidadeDisponivel=quantidadeDisponivel;
    }





    @Column(nullable = false)
    private Integer numeroDePaginas;

    public Integer getNumeroDePaginas(){
        return numeroDePaginas;
    }

    public void setNumeroDePaginas(Integer numeroDePaginas){
        this.numeroDePaginas=numeroDePaginas;
    }
}
