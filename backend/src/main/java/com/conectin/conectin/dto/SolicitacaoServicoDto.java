package com.conectin.conectin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitacaoServicoDto {

    @NotNull(message = "O ID do cliente é obrigatório")
    private Integer clienteId;

    @NotNull(message = "O ID do prestador é obrigatório")
    private Integer prestadorId;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Integer categoriaId;

    private String detalhes;
}