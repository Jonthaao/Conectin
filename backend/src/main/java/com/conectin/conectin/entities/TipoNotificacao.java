package com.conectin.conectin.entities; // ou um subpacote como .notifications

public enum TipoNotificacao {
    NOVA_SOLICITACAO_PARA_PRESTADOR,
    SOLICITACAO_ACEITA_PARA_CLIENTE,
    SOLICITACAO_RECUSADA_PARA_CLIENTE,
    // Outros tipos conforme necessário
    AVALIACAO_RECEBIDA,
    SOLICITACAO_CONCLUIDA_PARA_CLIENTE,
    SOLICITACAO_CANCELADA_PARA_PRESTADOR
    // MENSAGEM_CHAT (se tiver chat)
}