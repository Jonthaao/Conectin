package com.conectin.conectin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.conectin.conectin.dto.AvaliacaoDto;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.repository.AvaliacaoRepository;
import com.conectin.conectin.services.AvaliacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @PostMapping("/criar")
    public ResponseEntity<Avaliacao> criarAvaliacao(@Valid @RequestBody AvaliacaoDto dto) {
        try {
            Avaliacao avaliacao = avaliacaoService.criarAvaliacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
        } catch (IllegalArgumentException e) {
            // Idealmente, tratar exceções específicas e retornar mensagens de erro adequadas
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Exemplo simplificado
        } catch (RuntimeException e) { // Para erros como "Perfil não encontrado"
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Listar avaliações que o usuário com {usuarioId} recebeu COMO PRESTADOR
    @GetMapping("/recebidas/prestador/{usuarioId}")
    public ResponseEntity<List<Avaliacao>> listarAvaliacoesRecebidasComoPrestador(@PathVariable Long usuarioId) {
        try {
           List<Avaliacao> avaliacoes = avaliacaoService.listarAvaliacoesRecebidasComoPrestador(usuarioId);
           return ResponseEntity.ok(avaliacoes);
        } catch (IllegalArgumentException e) {
           return ResponseEntity.notFound().build();
        }
    }

    // Listar avaliações que o usuário com {usuarioId} recebeu COMO CLIENTE
    @GetMapping("/recebidas/cliente/{usuarioId}")
    public ResponseEntity<List<Avaliacao>> listarAvaliacoesRecebidasComoCliente(@PathVariable Long usuarioId) {
        try {
           List<Avaliacao> avaliacoes = avaliacaoService.listarAvaliacoesRecebidasComoCliente(usuarioId);
           return ResponseEntity.ok(avaliacoes);
        } catch (IllegalArgumentException e) {
           return ResponseEntity.notFound().build();
        }
    }

    // Seu endpoint antigo:
    // @GetMapping("/usuario/{usuarioId}")
    // public ResponseEntity<List<Avaliacao>> listarAvaliacoesPorUsuario(@PathVariable Long usuarioId) {
    //     // Este endpoint é genérico. Ele lista todas as avaliações onde o usuário foi o 'avaliado',
    //     // sem distinguir se foi como cliente ou prestador. Pode ser útil, mas os acima são mais específicos.
    //     List<Avaliacao> avaliacoes = avaliacaoRepository.findByAvaliadoId(usuarioId); // Supondo que o método no repo aceite Long
    //     return ResponseEntity.ok(avaliacoes);
    // }
}