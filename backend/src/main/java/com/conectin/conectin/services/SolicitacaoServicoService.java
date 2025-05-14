package com.conectin.conectin.services;

import com.conectin.conectin.dto.AtualizarStatusSolicitacaoDto;
import com.conectin.conectin.dto.CriarSolicitacaoServicoDto; // Importar o DTO correto
import com.conectin.conectin.dto.SolicitacaoServicoDto;
import com.conectin.conectin.dto.SolicitacaoServicoRespostaDto;
import com.conectin.conectin.dto.UsuarioResumoDto;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.StatusSolicitacao;
import com.conectin.conectin.entities.Usuario;
// import com.conectin.conectin.entities.Categoria; // Se mantiver
import com.conectin.conectin.repository.SolicitacaoServicoRepository;
import com.conectin.conectin.repository.UsuarioRepository;
// import com.conectin.conectin.repository.CategoriaRepository; // Se mantiver
// import com.conectin.conectin.services.NotificacaoService; // Para enviar notificações

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante!

import java.time.LocalDateTime;
import java.util.List; // Para listar
import java.util.stream.Collectors; // Para mapear para DTOs

@Service
public class SolicitacaoServicoService {

    @Autowired
    private SolicitacaoServicoRepository solicitacaoServicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // @Autowired
    // private CategoriaRepository categoriaRepository; // Se mantiver categoria

    @Autowired
    private NotificacaoService notificacaoService; // << INJETAR O SERVIÇO DE NOTIFICAÇÃO

    @Transactional
    public SolicitacaoServico criarSolicitacao(CriarSolicitacaoServicoDto dto, Usuario clienteLogado) {
        Usuario prestador = usuarioRepository.findById(dto.getPrestadorId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Prestador não encontrado com ID: " + dto.getPrestadorId()));

        if (!prestador.isPrestador()) {
            throw new IllegalArgumentException("O usuário selecionado não é um prestador.");
        }

        // Verificar se já existe uma solicitação PENDENTE_ACEITACAO do mesmo cliente
        // para o mesmo prestador
        // para o mesmo tipo de serviço (se aplicável) para evitar duplicidade imediata.
        List<SolicitacaoServico> existentesPendentes = solicitacaoServicoRepository
                .findByClienteIdAndPrestadorIdAndStatusOrderByDataHoraSolicitacaoDesc(
                        clienteLogado.getId(),
                        prestador.getId(),
                        StatusSolicitacao.PENDENTE_ACEITACAO); // Você precisará criar este método no repository.

        // Exemplo de como adicionar o método no SolicitacaoServicoRepository:
        // List<SolicitacaoServico>
        // findByClienteIdAndPrestadorIdAndStatusOrderByDataHoraSolicitacaoDesc(
        // Long clienteId, Long prestadorId, StatusSolicitacao status);

        if (!existentesPendentes.isEmpty()) {
            // Poderia verificar se a descrição do serviço é muito similar também
            throw new IllegalStateException(
                    "Você já possui uma solicitação pendente para este prestador com descrição similar ou recente.");
        }

        SolicitacaoServico solicitacao = new SolicitacaoServico();
        solicitacao.setCliente(clienteLogado);
        solicitacao.setPrestador(prestador);
        solicitacao.setDescricaoServico(dto.getDescricaoServico());
        solicitacao.setEnderecoServico(
                dto.getEnderecoServico() != null ? dto.getEnderecoServico() : clienteLogado.getEndereco());
        solicitacao.setObservacoesCliente(dto.getObservacoesCliente());
        // dataHoraSolicitacao e status (PENDENTE_ACEITACAO) são definidos pelo
        // @PrePersist

        SolicitacaoServico solicitacaoSalva = solicitacaoServicoRepository.save(solicitacao);

        // ---->>> ENVIAR NOTIFICAÇÃO PARA O PRESTADOR <<<----
        notificacaoService.notificarPrestadorNovaSolicitacao(solicitacaoSalva);

        return solicitacaoSalva;
    }

    public SolicitacaoServicoRespostaDto buscarPorIdDto(Integer id, Usuario usuarioLogado) {
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com ID: " + id));

        // Validação: O usuário logado é o cliente ou o prestador desta solicitação?
        if (solicitacao.getCliente().getId() != usuarioLogado.getId() &&
                solicitacao.getPrestador().getId() != usuarioLogado.getId()) {
            throw new SecurityException("Usuário não autorizado a visualizar esta solicitação.");
        }
        return convertToDto(solicitacao);
    }

    @Transactional
    public SolicitacaoServicoRespostaDto atualizarStatus(Integer solicitacaoId, AtualizarStatusSolicitacaoDto dto,
            Usuario usuarioLogado) {
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com ID: " + solicitacaoId));

        StatusSolicitacao novoStatus;
        try {
            novoStatus = StatusSolicitacao.valueOf(dto.getNovoStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Novo status inválido: " + dto.getNovoStatus());
        }

        boolean isPrestadorDaSolicitacao = solicitacao.getPrestador().getId().equals(usuarioLogado.getId());
        boolean isClienteDaSolicitacao = solicitacao.getCliente().getId().equals(usuarioLogado.getId());
        StatusSolicitacao statusAtual = solicitacao.getStatus();

        // Lógica de permissão para mudança de status
        switch (statusAtual) {
            case PENDENTE_ACEITACAO:
                if (isPrestadorDaSolicitacao) {
                    if (novoStatus == StatusSolicitacao.ACEITA || novoStatus == StatusSolicitacao.AGENDADA) {
                        // OK
                        if (dto.getObservacoesPrestador() != null) {
                            solicitacao.setObservacoesPrestador(dto.getObservacoesPrestador());
                        }
                        if (novoStatus == StatusSolicitacao.AGENDADA && dto.getDataHoraAgendamento() != null) {
                            solicitacao.setDataHoraAgendamento(dto.getDataHoraAgendamento());
                        } else if (novoStatus == StatusSolicitacao.AGENDADA && dto.getDataHoraAgendamento() == null) {
                            throw new IllegalArgumentException(
                                    "Data de agendamento é obrigatória para o status AGENDADA.");
                        }
                    } else if (novoStatus == StatusSolicitacao.RECUSADA_PRESTADOR) {
                        // OK
                        if (dto.getObservacoesPrestador() != null) {
                            solicitacao.setObservacoesPrestador(dto.getObservacoesPrestador());
                        } else {
                            // Pode tornar obrigatório um motivo para recusa
                            // throw new IllegalArgumentException("Observação do prestador é obrigatória ao
                            // recusar.");
                        }
                    } else {
                        throw new SecurityException(
                                "Mudança de status não permitida para este novo status pelo prestador.");
                    }
                } else if (isClienteDaSolicitacao && novoStatus == StatusSolicitacao.CANCELADA_CLIENTE) {
                    // OK
                    if (dto.getObservacoesCliente() != null) {
                        solicitacao.setObservacoesCliente(dto.getObservacoesCliente());
                    }
                } else {
                    throw new SecurityException(
                            "Mudança de status não permitida para PENDENTE_ACEITACAO por este usuário ou para este novo status.");
                }
                break;
            // ... (outros casos como ACEITA, AGENDADA, EM_ANDAMENTO) ...
            case ACEITA:
            case AGENDADA: // Se AGENDADA pode ir para EM_ANDAMENTO ou CONCLUIDA
                if (isPrestadorDaSolicitacao && (novoStatus == StatusSolicitacao.EM_ANDAMENTO
                        || novoStatus == StatusSolicitacao.CONCLUIDA)) {
                    // OK
                    if (novoStatus == StatusSolicitacao.CONCLUIDA) {
                        solicitacao.setDataHoraConclusao(LocalDateTime.now()); // Definir data de conclusão
                    }
                } else if (isClienteDaSolicitacao && novoStatus == StatusSolicitacao.CANCELADA_CLIENTE) {
                    // Regra de negócio: cliente pode cancelar se ainda não iniciou?
                    // if (statusAtual == StatusSolicitacao.EM_ANDAMENTO) {
                    // throw new SecurityException("A solicitação não pode ser cancelada pois já
                    // está em andamento.");
                    // }
                    // OK (se permitido)
                } else {
                    throw new SecurityException("Mudança de status não permitida para " + statusAtual
                            + " por este usuário ou para este novo status.");
                }
                break;
            case EM_ANDAMENTO:
                if (isPrestadorDaSolicitacao && novoStatus == StatusSolicitacao.CONCLUIDA) {
                    solicitacao.setDataHoraConclusao(LocalDateTime.now()); // Definir data de conclusão
                } else {
                    throw new SecurityException(
                            "Mudança de status não permitida para EM_ANDAMENTO por este usuário ou para este novo status.");
                }
                break;
            default:
                throw new IllegalStateException(
                        "Mudança de status não permitida a partir do status atual: " + solicitacao.getStatus());
        }

        solicitacao.setStatus(novoStatus);
        SolicitacaoServico solicitacaoAtualizada = solicitacaoServicoRepository.save(solicitacao);

        // ---->>> ENVIAR NOTIFICAÇÃO SOBRE A ATUALIZAÇÃO DE STATUS <<<----
        if (novoStatus == StatusSolicitacao.ACEITA || novoStatus == StatusSolicitacao.AGENDADA) {
            if (isPrestadorDaSolicitacao) { // Prestador aceitou/agendou
                notificacaoService.notificarClienteSolicitacaoAceita(solicitacaoAtualizada);
            }
        } else if (novoStatus == StatusSolicitacao.RECUSADA_PRESTADOR) {
            if (isPrestadorDaSolicitacao) { // Prestador recusou
                notificacaoService.notificarClienteSolicitacaoRecusada(solicitacaoAtualizada);
            }
        } else if (novoStatus == StatusSolicitacao.CANCELADA_CLIENTE) { // <<< NOVO
            if (isClienteDaSolicitacao) {
                notificacaoService.notificarPrestadorSolicitacaoCancelada(solicitacaoAtualizada);
            }
        } else if (novoStatus == StatusSolicitacao.CONCLUIDA) { // <<< NOVO
            if (isPrestadorDaSolicitacao) {
                notificacaoService.notificarClienteSolicitacaoConcluida(solicitacaoAtualizada);
            }
        }
        // ...
        return convertToDto(solicitacaoAtualizada);
    }

    public SolicitacaoServicoRespostaDto convertToDto(SolicitacaoServico solicitacao) {
        SolicitacaoServicoRespostaDto dto = new SolicitacaoServicoRespostaDto();
        dto.setId(solicitacao.getId());
        // Preencher os campos básicos como antes...
        dto.setDescricaoServico(solicitacao.getDescricaoServico()); // Usar o campo original
        dto.setDataHoraSolicitacao(solicitacao.getDataHoraSolicitacao());
        dto.setDataHoraAgendamento(solicitacao.getDataHoraAgendamento());
        dto.setStatus(solicitacao.getStatus().name());
        dto.setEnderecoServico(solicitacao.getEnderecoServico());
        dto.setObservacoesCliente(solicitacao.getObservacoesCliente());
        dto.setObservacoesPrestador(solicitacao.getObservacoesPrestador());

        if (solicitacao.getCliente() != null) {
            UsuarioResumoDto clienteDto = new UsuarioResumoDto();
            clienteDto.setId(solicitacao.getCliente().getId());
            clienteDto.setNome(solicitacao.getCliente().getNome());
            clienteDto.setFotoPerfil(solicitacao.getCliente().getFotoPerfil());
            if (solicitacao.getCliente().getCliente() != null) { // Perfil de cliente
                clienteDto.setAvaliacaoMediaComoCliente(solicitacao.getCliente().getCliente().getAvaliacaoMedia());
            }
            dto.setCliente(clienteDto);
        }

        if (solicitacao.getPrestador() != null) {
            UsuarioResumoDto prestadorDto = new UsuarioResumoDto();
            prestadorDto.setId(solicitacao.getPrestador().getId());
            prestadorDto.setNome(solicitacao.getPrestador().getNome());
            prestadorDto.setFotoPerfil(solicitacao.getPrestador().getFotoPerfil());
            if (solicitacao.getPrestador().getPrestador() != null) { // Perfil de prestador
                prestadorDto
                        .setAvaliacaoMediaComoPrestador(solicitacao.getPrestador().getPrestador().getAvaliacaoMedia());
            }
            dto.setPrestador(prestadorDto);
        }

        // Lógica de visibilidade de contato
        // Assumindo que Usuario tem um campo `telefone` (você precisará adicioná-lo se
        // não tiver)
        // Ex: private String telefone; na entidade Usuario

        // Adicione o campo 'telefone' na entidade Usuario, DTO de usuário, etc.
        // Aqui, estou simulando que ele existe.
        // Se você não tem um campo telefone direto no Usuario, mas talvez no Prestador
        // ou Cliente, ajuste.
        // Supondo que 'email' seja o contato por enquanto, se não tiver telefone.
        // Idealmente, Usuario teria 'telefone'.

        StatusSolicitacao status = solicitacao.getStatus();

        if (status == StatusSolicitacao.ACEITA ||
                status == StatusSolicitacao.AGENDADA ||
                status == StatusSolicitacao.EM_ANDAMENTO ||
                status == StatusSolicitacao.CONCLUIDA) {

            // Se o prestador aceitou, o contato do prestador fica visível para o cliente
            if (solicitacao.getPrestador() != null ) {
                // Suponha que Usuario tenha getTelefone() ou você pegue de Prestador.
                // dto.setContatoPrestadorParaCliente(solicitacao.getPrestador().getUsuario().getTelefone());
                // // IDEAL
                dto.setContatoPrestadorParaCliente("Ver contato no perfil do prestador"); // Ou uma mensagem
            }

            // E o contato do cliente fica visível para o prestador
            if (solicitacao.getCliente() != null) {
                // dto.setContatoClienteParaPrestador(solicitacao.getCliente().getTelefone());
                // // IDEAL
                dto.setContatoClienteParaPrestador(solicitacao.getCliente().getEmail()); // Exemplo usando email
            }
        } else {
            // Se PENDENTE_ACEITACAO ou RECUSADA, etc., os contatos podem não ser visíveis
            dto.setContatoClienteParaPrestador("Aguardando aceitação do prestador");
            dto.setContatoPrestadorParaCliente("Aguardando aceitação do prestador");
        }

        // TODO: Carregar avaliações se houver (você já tem essa lógica)
        // dto.setAvaliacoes(...);

        return dto;
    }

    public List<SolicitacaoServicoRespostaDto> listarSolicitacoesPorCliente(Long clienteId) {
        usuarioRepository.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        return solicitacaoServicoRepository.findByClienteIdOrderByDataHoraSolicitacaoDesc(clienteId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoServicoRespostaDto> listarSolicitacoesPorPrestador(Long prestadorId) {
        Usuario prestador = usuarioRepository.findById(prestadorId)
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado"));
        if (!prestador.isPrestador())
            throw new IllegalArgumentException("Usuário não é um prestador");
        return solicitacaoServicoRepository.findByPrestadorIdOrderByDataHoraSolicitacaoDesc(prestadorId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean existeSolicitacaoAceita(Long clienteId, Long prestadorId) {
        List<SolicitacaoServico> solicitacoes = solicitacaoServicoRepository
                .findByClienteIdAndPrestadorIdAndStatus(clienteId, prestadorId, "ACEITA");
        return !solicitacoes.isEmpty();
    }
}