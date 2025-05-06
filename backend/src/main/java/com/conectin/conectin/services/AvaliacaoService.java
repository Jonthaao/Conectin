package com.conectin.conectin.services;

import java.time.LocalDateTime;

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

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private SolicitacaoServicoRepository solicitacaoServicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    public Avaliacao criarAvaliacao(AvaliacaoDto dto) {
        SolicitacaoServico solicitacao = solicitacaoServicoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        Usuario avaliador = usuarioRepository.findById(dto.getAvaliadorId())
                .orElseThrow(() -> new IllegalArgumentException("Avaliador não encontrado"));
        Usuario avaliado = usuarioRepository.findById(dto.getAvaliadoId())
                .orElseThrow(() -> new IllegalArgumentException("Avaliado não encontrado"));

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setSolicitacao(solicitacao);
        avaliacao.setAvaliador(avaliador);
        avaliacao.setAvaliado(avaliado);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());
        avaliacao.setData(LocalDateTime.now());
        avaliacao.setFotos(dto.getFotos());

        avaliacao = avaliacaoRepository.save(avaliacao);

        // Atualizar a média de avaliações do usuário avaliado
        atualizarAvaliacaoMedia(avaliado);

        return avaliacao;
    }

    private void atualizarAvaliacaoMedia(Usuario avaliado) {
        Float media = avaliacaoRepository.calcularMediaAvaliacoes(avaliado.getId());
        if (media == null) {
            media = 0.0f; // Caso não haja avaliações
        }
    
        final Float finalMedia = media; // Criar uma cópia final
    
        // Verificar se o avaliado é um Cliente
        clienteRepository.findByUsuarioId(avaliado.getId()).ifPresent(cliente -> {
            cliente.setAvaliacaoMedia(finalMedia);
            clienteRepository.save(cliente);
        });
    
        // Verificar se o avaliado é um Prestador
        prestadorRepository.findByUsuarioId(avaliado.getId()).ifPresent(prestador -> {
            prestador.setAvaliacaoMedia(finalMedia);
            prestadorRepository.save(prestador);
        });
    }
}