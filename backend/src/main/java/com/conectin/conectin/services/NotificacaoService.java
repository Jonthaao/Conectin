package com.conectin.conectin.services; // ou .notifications

import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.entities.Notificacao;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.TipoNotificacao;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.NotificacaoRepository;
import com.conectin.conectin.repository.UsuarioRepository; // Necessário para buscar dados completos se precisar
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.conectin.conectin.dto.NotificacaoDto; // Importe seu DTO

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private EmailService emailService; // Injete o EmailService

    @Autowired
    private UsuarioRepository usuarioRepository; // Para buscar informações do remetente/destinatário se necessário

    @Transactional
    public Notificacao criarNotificacao(Usuario destinatario, Usuario remetente, SolicitacaoServico solicitacao, String mensagem, TipoNotificacao tipo, String link) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setRemetente(remetente); // Pode ser null
        notificacao.setSolicitacao(solicitacao); // Pode ser null
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setLink(link); // Ex: "/solicitacoes/" + solicitacao.getId()
        notificacao.setDataCriacao(LocalDateTime.now());
        
        Notificacao notificacaoSalva = notificacaoRepository.save(notificacao);
        logger.info("Notificação criada para {} (ID: {})", destinatario.getEmail(), notificacaoSalva.getId());
        return notificacaoSalva;
    }

    // --- Métodos específicos para os seus casos de uso ---

    @Transactional
    public void notificarPrestadorNovaSolicitacao(SolicitacaoServico solicitacao) {
        Usuario prestador = solicitacao.getPrestador();
        Usuario cliente = solicitacao.getCliente();

        String mensagemPlataforma = String.format("Você recebeu uma nova solicitação de serviço de %s.", cliente.getNome());
        String link = "/minhas-solicitacoes/prestador/" + solicitacao.getId(); // Exemplo de link
        
        Notificacao notificacao = criarNotificacao(prestador, cliente, solicitacao, mensagemPlataforma, TipoNotificacao.NOVA_SOLICITACAO_PARA_PRESTADOR, link);

        // Enviar email
        String assuntoEmail = "Nova Solicitação de Serviço Recebida";
        String textoEmail = String.format("Olá %s,\n\nVocê recebeu uma nova solicitação de serviço de %s para o serviço: %s.\n\nAcesse a plataforma para ver os detalhes: %s\n\nAtenciosamente,\nEquipe Conectin",
                prestador.getNome(), cliente.getNome(), solicitacao.getDescricaoServico(), "https://seusite.com" + link); // TODO: Ajustar URL base
        
        emailService.enviarEmailSimples(prestador.getEmail(), assuntoEmail, textoEmail);
        notificacao.setEnviadaPorEmail(true); // Marcar que foi enviada
        notificacaoRepository.save(notificacao); // Salvar a atualização do status de envio de email
    }

    @Transactional
    public void notificarClienteSolicitacaoAceita(SolicitacaoServico solicitacao) {
        Usuario cliente = solicitacao.getCliente();
        Usuario prestador = solicitacao.getPrestador();

        String mensagemPlataforma = String.format("Boas notícias! %s aceitou sua solicitação de serviço.", prestador.getNome());
        String link = "/minhas-solicitacoes/cliente/" + solicitacao.getId(); // Exemplo de link
        
        Notificacao notificacao = criarNotificacao(cliente, prestador, solicitacao, mensagemPlataforma, TipoNotificacao.SOLICITACAO_ACEITA_PARA_CLIENTE, link);

        // Enviar email
        String assuntoEmail = "Sua Solicitação de Serviço foi Aceita!";
        String textoEmail = String.format("Olá %s,\n\nSua solicitação de serviço para %s (%s) foi aceita!\n\nAgora você pode visualizar o contato do prestador e combinar os próximos passos.\n\nAcesse a plataforma para mais detalhes: %s\n\nAtenciosamente,\nEquipe Conectin",
                cliente.getNome(), prestador.getNome(), solicitacao.getDescricaoServico(), "https://seusite.com" + link); // TODO: Ajustar URL base

        emailService.enviarEmailSimples(cliente.getEmail(), assuntoEmail, textoEmail);
        notificacao.setEnviadaPorEmail(true);
        notificacaoRepository.save(notificacao);
    }
    
    @Transactional
    public void notificarClienteSolicitacaoRecusada(SolicitacaoServico solicitacao) {
        Usuario cliente = solicitacao.getCliente();
        Usuario prestador = solicitacao.getPrestador();

        String mensagemPlataforma = String.format("Sua solicitação para %s foi recusada. Motivo: %s", 
                                                  prestador.getNome(), 
                                                  solicitacao.getObservacoesPrestador() != null ? solicitacao.getObservacoesPrestador() : "Não especificado");
        String link = "/minhas-solicitacoes/cliente/" + solicitacao.getId();
        
        Notificacao notificacao = criarNotificacao(cliente, prestador, solicitacao, mensagemPlataforma, TipoNotificacao.SOLICITACAO_RECUSADA_PARA_CLIENTE, link);

        // Enviar email
        String assuntoEmail = "Atualização sobre sua Solicitação de Serviço";
        String textoEmail = String.format("Olá %s,\n\nInfelizmente, sua solicitação de serviço para %s (%s) foi recusada.\n\nMotivo informado pelo prestador: %s\n\nVocê pode buscar outros prestadores na plataforma.\n\nAtenciosamente,\nEquipe Conectin",
                cliente.getNome(), prestador.getNome(), solicitacao.getDescricaoServico(), 
                solicitacao.getObservacoesPrestador() != null ? solicitacao.getObservacoesPrestador() : "Não especificado");

        emailService.enviarEmailSimples(cliente.getEmail(), assuntoEmail, textoEmail);
        notificacao.setEnviadaPorEmail(true);
        notificacaoRepository.save(notificacao);
    }


    // --- Métodos para gerenciamento de notificações pelo usuário ---

    public List<NotificacaoDto> getNotificacoesParaUsuario(Usuario usuario) {
        List<Notificacao> notificacoes = notificacaoRepository.findByDestinatarioOrderByDataCriacaoDesc(usuario);
        return notificacoes.stream()
                           .map(this::convertToDto)
                           .collect(Collectors.toList());
    }

    public long getContagemNotificacoesNaoLidas(Usuario usuario) {
        return notificacaoRepository.countByDestinatarioAndDataLeituraIsNull(usuario);
    }

    @Transactional
    public NotificacaoDto marcarNotificacaoComoLida(Long notificacaoId, Usuario usuarioLogado) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada: " + notificacaoId));

        // Verifica se a notificação pertence ao usuário logado
        if (!notificacao.getDestinatario().getId().equals(usuarioLogado.getId())) {
            throw new SecurityException("Usuário não autorizado a marcar esta notificação como lida.");
        }

        if (notificacao.getDataLeitura() == null) {
            notificacao.setDataLeitura(LocalDateTime.now());
            notificacaoRepository.save(notificacao);
        }
        return convertToDto(notificacao);
    }
    
    @Transactional
    public void marcarTodasComoLidas(Usuario usuarioLogado) {
        List<Notificacao> naoLidas = notificacaoRepository.findByDestinatarioOrderByDataCriacaoDesc(usuarioLogado)
            .stream()
            .filter(n -> n.getDataLeitura() == null)
            .collect(Collectors.toList());

        for (Notificacao notificacao : naoLidas) {
            notificacao.setDataLeitura(LocalDateTime.now());
        }
        notificacaoRepository.saveAll(naoLidas); // Salva todas as modificações em lote
        logger.info("{} notificações marcadas como lidas para o usuário {}", naoLidas.size(), usuarioLogado.getEmail());
    }


    private NotificacaoDto convertToDto(Notificacao n) {
        return new NotificacaoDto(
            n.getId(),
            n.getDestinatario().getId(),
            n.getDestinatario().getNome(),
            n.getRemetente() != null ? n.getRemetente().getId() : null,
            n.getRemetente() != null ? n.getRemetente().getNome() : null,
            n.getSolicitacao() != null ? n.getSolicitacao().getId() : null,
            n.getMensagem(),
            n.getLink(),
            n.getTipo(),
            n.getDataCriacao(),
            n.getDataLeitura()
        );
    }

   @Transactional
public void notificarUsuarioAvaliado(Avaliacao avaliacao) {
    Usuario avaliado = avaliacao.getAvaliado();
    Usuario avaliador = avaliacao.getAvaliador();
    SolicitacaoServico solicitacao = avaliacao.getSolicitacao();

    String mensagem = String.format("Você recebeu uma nova avaliação de %s para o serviço '%s'.",
            avaliador.getNome(), solicitacao.getDescricaoServico());
    String link = "/avaliar-servico/" + solicitacao.getId(); // Link para visualizar a solicitação avaliada

    Notificacao notificacao = criarNotificacao(
            avaliado, avaliador, solicitacao, mensagem, TipoNotificacao.AVALIACAO_RECEBIDA, link);

    // Opcional: Enviar email
    String assuntoEmail = "Você recebeu uma nova avaliação!";
    String textoEmail = String.format("Olá %s,\n\n%s\n\nAcesse a plataforma para ver detalhes: %s\n\nAtenciosamente,\nEquipe Conectin",
            avaliado.getNome(), mensagem, "http://localhost:8080" + link);
    emailService.enviarEmailSimples(avaliado.getEmail(), assuntoEmail, textoEmail);
    notificacao.setEnviadaPorEmail(true);
    notificacaoRepository.save(notificacao);
}

@Transactional
public void notificarClienteSolicitacaoConcluida(SolicitacaoServico solicitacao) {
    Usuario cliente = solicitacao.getCliente();
    Usuario prestador = solicitacao.getPrestador();
    String mensagem = String.format("%s marcou o serviço '%s' (solicitação #%d) como concluído. Você já pode avaliá-lo!",
                                    prestador.getNome(), solicitacao.getDescricaoServico(), solicitacao.getId());
    String link = "/avaliar-servico/" + solicitacao.getId(); 
    
    criarNotificacao(cliente, prestador, solicitacao, mensagem, TipoNotificacao.SOLICITACAO_CONCLUIDA_PARA_CLIENTE, link);
    // Enviar email, se desejado
}

@Transactional
public void notificarPrestadorSolicitacaoCancelada(SolicitacaoServico solicitacao) {
    Usuario prestador = solicitacao.getPrestador();
    Usuario cliente = solicitacao.getCliente();
    String mensagem = String.format("%s cancelou a solicitação de serviço #%d: %s.",
                                    cliente.getNome(), solicitacao.getId(), solicitacao.getDescricaoServico());
    String link = "/solicitacao/" + solicitacao.getId(); 
    
    criarNotificacao(prestador, cliente, solicitacao, mensagem, TipoNotificacao.SOLICITACAO_CANCELADA_PARA_PRESTADOR, link);
    // Enviar email, se desejado
}

public void notificarInteresse(Long prestadorId, Long clienteId) {
        Usuario prestador = usuarioRepository.findById(prestadorId)
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado"));
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(prestador);
        notificacao.setRemetente(cliente);
        notificacao.setMensagem("Um cliente está interessado em seus serviços.");
        notificacao.setDataCriacao(LocalDateTime.now());

        notificacaoRepository.save(notificacao);
    }
}