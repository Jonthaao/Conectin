package com.conectin.conectin.entities; // ou .notifications

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor // Lombok para construtor padrão
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_usuario_id", nullable = false)
    private Usuario destinatario; // Usuário que receberá a notificação

    @ManyToOne(fetch = FetchType.LAZY) // Opcional: quem originou a ação que gerou a notificação
    @JoinColumn(name = "remetente_usuario_id")
    private Usuario remetente;

    @ManyToOne(fetch = FetchType.LAZY) // Opcional: linkar com a solicitação relacionada
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoServico solicitacao;

    @Column(nullable = false)
    private String mensagem;

    private String link; // Opcional: um link para onde o usuário deve ser redirecionado ao clicar na notificação

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipo; // Opcional, para categorizar

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataLeitura; // Para marcar se foi lida

    private boolean enviadaPorEmail; // Controle se já foi enviada por email

    public Notificacao(Usuario destinatario, String mensagem, TipoNotificacao tipo, SolicitacaoServico solicitacao, Usuario remetente) {
        this.destinatario = destinatario;
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.solicitacao = solicitacao;
        this.remetente = remetente;
        this.dataCriacao = LocalDateTime.now();
        this.enviadaPorEmail = false; // Default
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }
}