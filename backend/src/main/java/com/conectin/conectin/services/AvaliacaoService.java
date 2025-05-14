package com.conectin.conectin.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.AvaliacaoDto;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.entities.Cliente;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.AvaliacaoRepository;
import com.conectin.conectin.repository.ClienteRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.repository.SolicitacaoServicoRepository;
import com.conectin.conectin.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SolicitacaoServicoRepository solicitacaoServicoRepository; // Supondo que você tenha este repositório

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private NotificacaoService notificacaoService; // Para enviar notificações

    @Transactional
    public Avaliacao criarAvaliacao(AvaliacaoDto dto) {
        // Validações
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação de serviço não encontrada com ID: " + dto.getSolicitacaoId()));

        Usuario avaliador = usuarioRepository.findById(dto.getAvaliadorId().longValue()) // Ajuste para Long se ID de Usuario for Long
                .orElseThrow(() -> new IllegalArgumentException("Usuário avaliador não encontrado com ID: " + dto.getAvaliadorId()));

        Usuario avaliado = usuarioRepository.findById(dto.getAvaliadoId().longValue()) // Ajuste para Long se ID de Usuario for Long
                .orElseThrow(() -> new IllegalArgumentException("Usuário avaliado não encontrado com ID: " + dto.getAvaliadoId()));

        // Verifica se o avaliador e avaliado são os participantes corretos da solicitação
        boolean avaliadorEhCliente = solicitacao.getCliente().getId() == avaliador.getId();
        boolean avaliadorEhPrestador = solicitacao.getPrestador().getId() == avaliador.getId();
        boolean avaliadoEhCliente = solicitacao.getCliente().getId() == avaliado.getId();
        boolean avaliadoEhPrestador = solicitacao.getPrestador().getId() == avaliado.getId();

        if (!((avaliadorEhCliente && avaliadoEhPrestador) || (avaliadorEhPrestador && avaliadoEhCliente))) {
            throw new IllegalArgumentException("Avaliador e/ou Avaliado não correspondem aos participantes da solicitação de serviço.");
        }

        // Verifica se já existe uma avaliação para esta combinação (opcional, mas bom)
        // Ex: um usuário só pode avaliar o outro uma vez por solicitação
        List<Avaliacao> existentes = avaliacaoRepository.findBySolicitacaoAndAvaliador(solicitacao, avaliador);
        if (!existentes.isEmpty()) {
            throw new IllegalArgumentException("Este usuário já avaliou esta solicitação.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setSolicitacao(solicitacao);
        avaliacao.setAvaliador(avaliador);
        avaliacao.setAvaliado(avaliado);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());
        avaliacao.setData(LocalDateTime.now());
        avaliacao.setFotos(dto.getFotos());

        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);

        // Atualizar a média do usuário avaliado
        atualizarMediaAvaliacaoUsuario(avaliado, solicitacao);

        // Enviar notificação ao usuário avaliado
        notificacaoService.notificarUsuarioAvaliado(avaliacaoSalva);

        return avaliacaoSalva;
    }

    @Transactional
    public void atualizarMediaAvaliacaoUsuario(Usuario usuarioAvaliado, SolicitacaoServico solicitacaoReferencia) {
        // Verifica se o usuário avaliado era o PRESTADOR na solicitação
        if (solicitacaoReferencia.getPrestador().getId() == usuarioAvaliado.getId()) {
            Prestador prestadorPerfil = prestadorRepository.findByUsuarioId(usuarioAvaliado.getId())
                    .orElseThrow(() -> new RuntimeException("Perfil de prestador não encontrado para o usuário: " + usuarioAvaliado.getId()));

            Float novaMedia = avaliacaoRepository.calcularMediaNotasParaUsuarioComoPrestador(usuarioAvaliado.getId());
            prestadorPerfil.setAvaliacaoMedia(novaMedia != null ? novaMedia : 0.0f); // Evita NPE se não houver notas
            prestadorRepository.save(prestadorPerfil);

        }
        // Verifica se o usuário avaliado era o CLIENTE na solicitação
        else if (solicitacaoReferencia.getCliente().getId() == usuarioAvaliado.getId()) {
            Cliente clientePerfil = clienteRepository.findByUsuarioId(usuarioAvaliado.getId())
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente não encontrado para o usuário: " + usuarioAvaliado.getId()));

            Float novaMedia = avaliacaoRepository.calcularMediaNotasParaUsuarioComoCliente(usuarioAvaliado.getId());
            clientePerfil.setAvaliacaoMedia(novaMedia != null ? novaMedia : 0.0f); // Evita NPE se não houver notas
            clienteRepository.save(clientePerfil);
        }
    }

     // Método para listar avaliações recebidas por um usuário como prestador
     public List<Avaliacao> listarAvaliacoesRecebidasComoPrestador(Long usuarioId) {
         usuarioRepository.findById(usuarioId) // Verifica se o usuário existe
                 .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));
         return avaliacaoRepository.findAvaliacoesRecebidasComoPrestador(usuarioId);
     }

     // Método para listar avaliações recebidas por um usuário como cliente
     public List<Avaliacao> listarAvaliacoesRecebidasComoCliente(Long usuarioId) {
         usuarioRepository.findById(usuarioId) // Verifica se o usuário existe
                 .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));
         return avaliacaoRepository.findAvaliacoesRecebidasComoCliente(usuarioId);
     }

//      @Transactional
// public Avaliacao criarAvaliacao(AvaliacaoDto dto) {
//     // ... (seu código existente para buscar entidades e validar) ...
//     Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);
//     atualizarMediaAvaliacaoUsuario(avaliado, solicitacao); // Seu método existente

//     notificacaoService.notificarUsuarioAvaliacaoRecebida(avaliacaoSalva); // <<< ADICIONAR AQUI

//     return avaliacaoSalva;
// }
}