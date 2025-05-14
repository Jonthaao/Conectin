package com.conectin.conectin.dto; // ou .notifications

import java.time.LocalDateTime;
import com.conectin.conectin.entities.TipoNotificacao; // Ajuste o import
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificacaoDto {
    private Long id;
    private Long destinatarioId;
    private String nomeDestinatario; // Útil para exibir
    private Long remetenteId; // Opcional
    private String nomeRemetente; // Opcional
    private Integer solicitacaoId; // Opcional
    private String mensagem;
    private String link; // Opcional
    private TipoNotificacao tipo; // Opcional
    private LocalDateTime dataCriacao;
    private LocalDateTime dataLeitura;
    private boolean lida;

    // Construtor para facilitar a conversão da entidade
    public NotificacaoDto(Long id, Long destinatarioId, String nomeDestinatario,
                          Long remetenteId, String nomeRemetente, Integer solicitacaoId,
                          String mensagem, String link, TipoNotificacao tipo,
                          LocalDateTime dataCriacao, LocalDateTime dataLeitura) {
        this.id = id;
        this.destinatarioId = destinatarioId;
        this.nomeDestinatario = nomeDestinatario;
        this.remetenteId = remetenteId;
        this.nomeRemetente = nomeRemetente;
        this.solicitacaoId = solicitacaoId;
        this.mensagem = mensagem;
        this.link = link;
        this.tipo = tipo;
        this.dataCriacao = dataCriacao;
        this.dataLeitura = dataLeitura;
        this.lida = (dataLeitura != null);
    }
}