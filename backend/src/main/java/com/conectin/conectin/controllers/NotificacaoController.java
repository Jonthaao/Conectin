package com.conectin.conectin.controllers;

import com.conectin.conectin.config.JwtUtil; // Seu utilitário JWT
import com.conectin.conectin.dto.NotificacaoDto;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.exception.ErrorMessages;
import com.conectin.conectin.repository.UsuarioRepository;
import com.conectin.conectin.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Para a contagem

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Usuario getUsuarioLogado(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }
        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken); // email
        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("Usuário não encontrado para o token fornecido.", "USER_NOT_FOUND_FOR_TOKEN"));
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDto>> getMinhasNotificacoes(@RequestHeader("Authorization") String token) {
        Usuario usuarioLogado = getUsuarioLogado(token);
        List<NotificacaoDto> notificacoes = notificacaoService.getNotificacoesParaUsuario(usuarioLogado);
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/contagem-nao-lidas")
    public ResponseEntity<Map<String, Long>> getContagemNaoLidas(@RequestHeader("Authorization") String token) {
        Usuario usuarioLogado = getUsuarioLogado(token);
        long contagem = notificacaoService.getContagemNotificacoesNaoLidas(usuarioLogado);
        return ResponseEntity.ok(Map.of("naoLidas", contagem));
    }

    @PostMapping("/{idNotificacao}/marcar-lida")
    public ResponseEntity<NotificacaoDto> marcarComoLida(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long idNotificacao) {
        Usuario usuarioLogado = getUsuarioLogado(token);
        try {
            NotificacaoDto notificacaoDto = notificacaoService.marcarNotificacaoComoLida(idNotificacao, usuarioLogado);
            return ResponseEntity.ok(notificacaoDto);
        } catch (IllegalArgumentException e) {
            throw new CustomException(e.getMessage(), "NOTIFICACAO_NAO_ENCONTRADA");
        } catch (SecurityException e) {
            throw new CustomException(e.getMessage(), "ACAO_NAO_AUTORIZADA");
        }
    }
    
    @PostMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(@RequestHeader("Authorization") String token) {
        Usuario usuarioLogado = getUsuarioLogado(token);
        try {
            notificacaoService.marcarTodasComoLidas(usuarioLogado);
            return ResponseEntity.ok().build();
        } catch (Exception e) { // Tratar exceções mais específicas se necessário
            throw new CustomException("Erro ao marcar todas as notificações como lidas.", "ERRO_MARCAR_TODAS_LIDAS");
        }
    }

    @PostMapping("/interesse")
    public ResponseEntity<String> enviarNotificacaoInteresse(
            @RequestParam Long prestadorId, 
            @RequestParam Long clienteId) {
        try {
            notificacaoService.notificarInteresse(prestadorId, clienteId);
            return ResponseEntity.ok("Notificação enviada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar notificação.");
        }
    }
}