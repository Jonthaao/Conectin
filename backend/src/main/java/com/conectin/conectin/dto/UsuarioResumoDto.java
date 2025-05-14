package com.conectin.conectin.dto;

import lombok.Data;

@Data
public class UsuarioResumoDto {
    private Long id;
    private String nome;
    private String fotoPerfil;
    // Para o prestador ver a avaliação do cliente:
    private Float avaliacaoMediaComoCliente; // Preenchido se o resumo for de um cliente e ele tiver perfil Cliente
    // Para o cliente ver a avaliação do prestador (embora ele já esteja no perfil do prestador):
    private Float avaliacaoMediaComoPrestador; // Preenchido se o resumo for de um prestador e ele tiver perfil Prestador
}
