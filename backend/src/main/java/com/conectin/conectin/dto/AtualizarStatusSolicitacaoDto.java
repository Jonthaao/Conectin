package com.conectin.conectin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AtualizarStatusSolicitacaoDto {
    @NotBlank(message = "O novo status é obrigatório")
    private String novoStatus; // Ex: "ACEITA", "RECUSADA_PRESTADOR", "CONCLUIDA"
    private String observacoesPrestador; // Se o prestador estiver atualizando (ex: motivo da recusa)
    private String observacoesCliente;   // Se o cliente estiver atualizando (ex: motivo do cancelamento)
    private LocalDateTime dataHoraAgendamento; // Se o novo status for AGENDADA
}