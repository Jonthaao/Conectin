package com.conectin.conectin.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SolicitacaoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) // Boa prática para performance
    @JoinColumn(name = "cliente_usuario_id", nullable = false) // Nome da coluna mais explícito
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_usuario_id", nullable = false) // Nome da coluna mais explícito
    private Usuario prestador;

    // Removido Categoria daqui, conforme sugestão anterior. Se precisar, adicione de volta.
    // @ManyToOne
    // @JoinColumn(name = "categoria_id") // nullable = false se for obrigatório
    // private Categoria categoria;

    @Column(nullable = false, length = 1000) // Campo para a descrição detalhada
    private String descricaoServico; // <- SEU CAMPO "detalhes" RENOMEADO E MELHORADO

    @Column(nullable = false)
    private LocalDateTime dataHoraSolicitacao; // <- SEU CAMPO "dataSolicitacao" RENOMEADO

    private LocalDateTime dataHoraAgendamento; // Novo campo sugerido

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(length = 255) // Novo campo sugerido
    private String enderecoServico;

    @Column(length = 500) // Novo campo sugerido
    private String observacoesCliente;

    @Column(length = 500) // Novo campo sugerido
    private String observacoesPrestador;

    private LocalDateTime dataHoraConclusao; // <- SEU CAMPO "dataConclusao" RENOMEADO

    // Opcional, mas recomendado para ligar avaliações à solicitação
    // @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Avaliacao> avaliacoes;

    @PrePersist
    protected void onCreate() {
        if (this.dataHoraSolicitacao == null) { // Para garantir que só defina se não estiver já setado
            this.dataHoraSolicitacao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusSolicitacao.PENDENTE_ACEITACAO; // Use o enum completo
        }
    }

    public String detalhes() {  // Método para retornar detalhes da solicitação
        return String.format("Solicitação de %s para %s em %s", cliente.getNome(), prestador.getNome(), descricaoServico);
    }
}