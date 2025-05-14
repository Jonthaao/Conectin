package com.conectin.conectin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitacaoServicoDto {

    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "O ID do prestador é obrigatório")
    private Long prestadorId;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Integer categoriaId;

    private String detalhes;
}