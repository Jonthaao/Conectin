package com.conectin.conectin.entities;

public enum StatusSolicitacao {
    PENDENTE_ACEITACAO, // Cliente solicitou, aguardando prestador (seu PENDENTE)
    ACEITA,             // Prestador aceitou
    RECUSADA_PRESTADOR, // Prestador recusou
    CANCELADA_CLIENTE,  // Cliente cancelou antes da aceitação/execução
    AGENDADA,           // Prestador aceitou e marcou data/hora
    EM_ANDAMENTO,       // Serviço sendo executado
    CONCLUIDA,          // Serviço finalizado, pronto para avaliação
    AVALIADA,           // (Opcional) Se quiser marcar que já foi avaliada por ambos ou um
    PROBLEMA            // Houve algum problema reportado
}