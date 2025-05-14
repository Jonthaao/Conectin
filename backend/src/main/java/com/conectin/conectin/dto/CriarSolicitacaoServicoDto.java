package com.conectin.conectin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CriarSolicitacaoServicoDto {
    @NotNull(message = "ID do prestador é obrigatório")
    private Long prestadorId; // ID do Usuario que é o prestador

    // @NotNull(message = "ID da categoria é obrigatório") // Se você decidir manter a categoria na solicitação
    // private Integer categoriaId;

    @NotBlank(message = "A descrição do serviço é obrigatória")
    private String descricaoServico; // Seus "detalhes"

    private String enderecoServico; // Opcional
    private String observacoesCliente; // Opcional
}