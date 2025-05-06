package com.conectin.conectin.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(nullable = false)
    private String titulo;

    private String descricao;

    @ElementCollection
    @CollectionTable(name = "portfolio_fotos", joinColumns = @JoinColumn(name = "portfolio_id"))
    @Column(name = "foto_url")
    private List<String> fotos;

    @Column(nullable = false)
    private LocalDateTime data;
}