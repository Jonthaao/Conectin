package com.conectin.conectin.dto;

import com.conectin.conectin.entities.TipoUsuario;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioDto {
    @NotBlank
    private String nome;

    @NotBlank
    private String endereco;

    @NotBlank
    private String email;

    @NotBlank
    private String senha;

    @NotBlank
    private String confirmarSenha;

    private TipoUsuario tipoUsuario;
}
