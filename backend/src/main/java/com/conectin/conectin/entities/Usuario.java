package com.conectin.conectin.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String fotoPerfil;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @ElementCollection(targetClass = TipoUsuario.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<TipoUsuario> tipos = new HashSet<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Prestador prestador;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cliente cliente;

    @PrePersist
    public void preencherDataCadastro() {
        this.dataCadastro = LocalDateTime.now();
    }

    public boolean isPrestador() {
        return tipos.contains(TipoUsuario.PRESTADOR);
    }

    public boolean isCliente() {
        return tipos.contains(TipoUsuario.CLIENTE);
    }

    public void addTipo(TipoUsuario tipo) {
        tipos.add(tipo);
    }
}