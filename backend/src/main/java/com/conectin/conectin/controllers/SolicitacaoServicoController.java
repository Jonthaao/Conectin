package com.conectin.conectin.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.config.JwtUtil;
import com.conectin.conectin.dto.AtualizarStatusSolicitacaoDto;
import com.conectin.conectin.dto.CriarSolicitacaoServicoDto;
import com.conectin.conectin.dto.SolicitacaoServicoDto;
import com.conectin.conectin.dto.SolicitacaoServicoRespostaDto;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.exception.ErrorMessages;
import com.conectin.conectin.repository.UsuarioRepository;
import com.conectin.conectin.services.SolicitacaoServicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoServicoController {

    @Autowired
    private SolicitacaoServicoService solicitacaoServicoService;
    @Autowired
    private UsuarioRepository usuarioRepository; // Para buscar o usuário logado

    @Autowired
    private JwtUtil jwtUtil; // Para extrair o usuário do token

    private Usuario getUsuarioLogado(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }
        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);
        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("Usuário cliente não encontrado para o token.",
                        "CLIENT_USER_NOT_FOUND"));
    }

    @PostMapping("/criar")
    public ResponseEntity<?> criarSolicitacao(@RequestHeader("Authorization") String token,
            @Valid @RequestBody CriarSolicitacaoServicoDto dto) {
        try {
            Usuario clienteLogado = getUsuarioLogado(token);
            if (!clienteLogado.isCliente()) { // Validação adicional
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Apenas clientes podem criar solicitações.", "code",
                                "SOLICITACAO_FORBIDDEN_CLIENTE"));
            }
            SolicitacaoServico solicitacao = solicitacaoServicoService.criarSolicitacao(dto, clienteLogado);
            // Você pode retornar a solicitação completa ou um DTO de resposta mais simples
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoServicoService.convertToDto(solicitacao)); // Usando
                                                                                                                        // o
                                                                                                                        // convertToDto
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "code", "SOLICITACAO_INVALID_DATA"));
        } catch (CustomException e) { // Se getUsuarioLogado lançar CustomException
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage(), "code", e.getErrorCode()));
        }
    }

    // Listar solicitações que o CLIENTE LOGADO FEZ
    @GetMapping("/minhas/cliente")
    public ResponseEntity<List<SolicitacaoServicoRespostaDto>> listarMinhasSolicitacoesComoCliente(
            @RequestHeader("Authorization") String token) {
        Usuario clienteLogado = getUsuarioLogado(token);
        List<SolicitacaoServicoRespostaDto> dtos = solicitacaoServicoService
                .listarSolicitacoesPorCliente(clienteLogado.getId());
        return ResponseEntity.ok(dtos);
    }

    // Listar solicitações que o PRESTADOR LOGADO RECEBEU
    @GetMapping("/minhas/prestador")
    public ResponseEntity<List<SolicitacaoServicoRespostaDto>> listarMinhasSolicitacoesComoPrestador(
            @RequestHeader("Authorization") String token) {
        Usuario prestadorLogado = getUsuarioLogado(token);
        if (!prestadorLogado.isPrestador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(List.of()); // Ou Map.of("message", "Apenas prestadores podem acessar.")
        }
        List<SolicitacaoServicoRespostaDto> dtos = solicitacaoServicoService
                .listarSolicitacoesPorPrestador(prestadorLogado.getId());
        return ResponseEntity.ok(dtos);
    }

    // Obter detalhes de UMA solicitação (para cliente ou prestador participante)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSolicitacaoPorId(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        Usuario usuarioLogado = getUsuarioLogado(token);
        try {
            SolicitacaoServicoRespostaDto dto = solicitacaoServicoService.buscarPorIdDto(id, usuarioLogado); // buscarPorIdDto
                                                                                                             // já
                                                                                                             // valida
                                                                                                             // permissão
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) { // Solicitacao nao encontrada
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) { // Usuario nao autorizado
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage(), "code", e.getErrorCode()));
        }
    }

    @PutMapping("/{id}/atualizar-status")
    public ResponseEntity<?> atualizarStatusSolicitacao(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarStatusSolicitacaoDto dto) {
        Usuario usuarioLogado = getUsuarioLogado(token);
        try {
            SolicitacaoServicoRespostaDto solicitacaoAtualizada = solicitacaoServicoService.atualizarStatus(id, dto,
                    usuarioLogado);
            return ResponseEntity.ok(solicitacaoAtualizada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) { // Não autorizado a mudar o status
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage(), "code", e.getErrorCode()));
        }
    }
    @GetMapping("/cliente/{clienteId}/prestador/{prestadorId}/aceita")
    public ResponseEntity<Map<String, Boolean>> verificarSolicitacaoAceita(
            @PathVariable Long clienteId, 
            @PathVariable Long prestadorId) {
        boolean aceita = solicitacaoServicoService.existeSolicitacaoAceita(clienteId, prestadorId);
        return ResponseEntity.ok(Map.of("aceita", aceita));
    }
}