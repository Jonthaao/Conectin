package com.conectin.conectin.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SolicitacaoServicoRespostaDto {
    private Integer id;
    private UsuarioResumoDto cliente;
    private UsuarioResumoDto prestador;
    private String descricaoServico;
    private LocalDateTime dataHoraSolicitacao;
    private LocalDateTime dataHoraAgendamento;
    private String status; // Enum StatusSolicitacao.name()
    private String enderecoServico;
    private String observacoesCliente;
    private String observacoesPrestador;
    private List<AvaliacaoDto> avaliacoes;

    // NOVOS CAMPOS PARA CONTATO CONDICIONAL
    private String contatoClienteParaPrestador; // Telefone do cliente, visível para prestador após ACEITA
    private String contatoPrestadorParaCliente; // Telefone do prestador, visível para cliente após ACEITA
}