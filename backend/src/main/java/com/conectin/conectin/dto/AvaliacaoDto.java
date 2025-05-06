package com.conectin.conectin.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvaliacaoDto {

    @NotNull(message = "O ID da solicitação é obrigatório")
    private Integer solicitacaoId;

    @NotNull(message = "O ID do avaliador é obrigatório")
    private Integer avaliadorId;

    @NotNull(message = "O ID do avaliado é obrigatório")
    private Integer avaliadoId;

    @Min(value = 1, message = "A nota deve ser no mínimo 1")
    @Max(value = 5, message = "A nota deve ser no máximo 5")
    private Float nota; // Alterado de Integer para Float

    private String comentario;

    private List<String> fotos;
}