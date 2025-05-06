package com.conectin.conectin.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import com.conectin.conectin.config.JwtUtil;
import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Cliente;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.TipoUsuario;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.exception.CustomException;
import com.conectin.conectin.exception.ErrorMessages;
import com.conectin.conectin.payload.SuccessMessage;
import com.conectin.conectin.repository.ClienteRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.repository.UsuarioRepository;
import com.conectin.conectin.services.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            Usuario usuario = usuarioService.cadastrarUsuario(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessMessage("Usuário cadastrado com sucesso: " + usuario.getNome(), "USER_SUCCESS_002"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("e-mail já existe")) {
                throw new CustomException(ErrorMessages.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS_CODE);
            } else if (e.getMessage().contains("e-mail inválido")) {
                throw new CustomException(ErrorMessages.INVALID_EMAIL, ErrorMessages.INVALID_EMAIL_CODE);
            } else if (e.getMessage().contains("usuário já existe")) {
                throw new CustomException(ErrorMessages.USER_ALREADY_EXISTS, ErrorMessages.USER_ALREADY_EXISTS_CODE);
            }
            throw new CustomException("Erro ao cadastrar usuário", "USER_ERROR_006");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioDto usuarioDto) {
        try {
            String email = usuarioDto.getEmail();
            String senha = usuarioDto.getSenha();
            if (email == null || senha == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Email e senha são obrigatórios");
                errorResponse.put("code", "USER_ERROR_001");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Usuario usuario = usuarioService.findByEmail(email);
            if (usuario == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Usuário não encontrado");
                errorResponse.put("code", "USER_ERROR_002");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            boolean isPasswordValid = BCrypt.checkpw(senha, usuario.getSenha());
            if (!isPasswordValid) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Usuário ou Senha incorreta");
                errorResponse.put("code", "USER_ERROR_003");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String token = jwtUtil.generateToken(usuario.getEmail());
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Login bem-sucedido!");
            successResponse.put("token", token);
            successResponse.put("code", "AUTH_SUCCESS_001");
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro no servidor: " + e.getMessage());
            errorResponse.put("code", "SERVER_ERROR_001");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfilUsuario(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }
        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);
        if (usuarioOpt.isEmpty()) {
            throw new CustomException("Usuário não encontrado", "USER_NOT_FOUND");
        }
        Usuario usuario = usuarioOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getId());
        response.put("nome", usuario.getNome());
        response.put("endereco", usuario.getEndereco());
        response.put("email", usuario.getEmail());
        response.put("foto", usuario.getFotoPerfil());
        response.put("tipos", usuario.getTipos());

        if (usuario.isPrestador()) {
            Optional<Prestador> prestadorOpt = prestadorRepository.findByUsuarioId(usuario.getId());
            if (prestadorOpt.isPresent()) {
                Prestador prestador = prestadorOpt.get();
                Map<String, Object> prestadorData = new HashMap<>();
                prestadorData.put("id", prestador.getId());
                prestadorData.put("descricao", prestador.getDescricao());
                prestadorData.put("disponibilidade", prestador.getDisponibilidade());
                prestadorData.put("avaliacaoMedia", prestador.getAvaliacaoMedia());
                response.put("prestador", prestadorData);
            }
        }

        if (usuario.isCliente()) {
            Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioId(usuario.getId());
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                Map<String, Object> clienteData = new HashMap<>();
                clienteData.put("id", cliente.getId());
                clienteData.put("historicoContratacoes", cliente.getHistoricoContratacoes());
                response.put("cliente", clienteData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto usuarioDto, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        try {
            Optional<Usuario> usuarioEditado = usuarioService.editarUsuario(id, usuarioDto);
            if (usuarioEditado.isEmpty()) {
                throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
            }
            return ResponseEntity.ok(new SuccessMessage("Usuário editado com sucesso: " + usuarioEditado.get().getNome(), "USER_SUCCESS_003"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("e-mail já existe")) {
                throw new CustomException(ErrorMessages.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS_CODE);
            } else if (e.getMessage().contains("e-mail inválido")) {
                throw new CustomException(ErrorMessages.INVALID_EMAIL, ErrorMessages.INVALID_EMAIL_CODE);
            }
            throw new CustomException("Erro ao editar usuário", "USER_ERROR_007");
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        boolean deletado = usuarioService.deletarUsuario(id);
        if (!deletado) {
            throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
        }
        return ResponseEntity.ok(new SuccessMessage("Usuário deletado com sucesso", "USER_SUCCESS_004"));
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarUsuarios(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }

        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken);

        if (!jwtUtil.validateToken(jwtToken, username)) {
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }

        java.util.List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}