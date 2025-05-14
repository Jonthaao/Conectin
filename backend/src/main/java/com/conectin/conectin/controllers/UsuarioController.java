package com.conectin.conectin.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private JwtUtil jwtUtil;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            Usuario usuario = usuarioService.cadastrarUsuario(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessMessage("Usuário cadastrado com sucesso: " + usuario.getNome(),
                            "USER_SUCCESS_002"));
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
    private Usuario getUsuarioLogado(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
        }
        String jwtToken = token.substring(7);
        String username = jwtUtil.extractUsername(jwtToken); // email
        if (!jwtUtil.validateToken(jwtToken, username)) { // Valida o token em si
            throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
        }
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("Usuário não encontrado para o token fornecido.", "USER_NOT_FOUND_FOR_TOKEN"));
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
                
                List<Map<String, Object>> categoriasList = prestador.getPrestadorCategorias().stream()
                        .map(pc -> {
                            Map<String, Object> catMap = new HashMap<>();
                            catMap.put("id", pc.getCategoria().getId().longValue());
                            catMap.put("nome", pc.getCategoria().getNome()); // Incluir nome é útil
                            return catMap;
                        })
                        .collect(Collectors.toList());
                prestadorData.put("categorias", categoriasList); 

                List<Map<String, Object>> cidadesList = prestador.getCidadePrestadores().stream()
                        .map(cp -> {
                            Map<String, Object> cidMap = new HashMap<>();
                            cidMap.put("id", cp.getCidade().getId().longValue());
                            cidMap.put("nome", cp.getCidade().getNome());
                            return cidMap;
                        })
                        .collect(Collectors.toList());
                prestadorData.put("cidades", cidadesList); // Adiciona a lista ao mapa do prestador
                
                List<Map<String, Object>> portfoliosList = prestador.getPortfolios().stream()
                        .map(p -> {
                            Map<String, Object> portMap = new HashMap<>();
                            portMap.put("id", p.getId()); // Inclui o ID do portfolio
                            portMap.put("descricao", p.getDescricao());
                            String urlImagem = (p.getFotos() != null && !p.getFotos().isEmpty()) ? p.getFotos().get(0)
                                    : null;
                            portMap.put("urlImagem", urlImagem); // Campo esperado pelo frontend
                            // Se precisar enviar a lista completa de fotos:
                            // portMap.put("fotos", p.getFotos());
                            return portMap;
                        })
                        .collect(Collectors.toList());
                prestadorData.put("portfolios", portfoliosList); // Adiciona a lista ao mapa do prestador

                response.put("prestador", prestadorData); // Adiciona o mapa completo do prestador à resposta principal
            } else {
                // Se for prestador mas não encontrar a entidade Prestador (inconsistência?),
                // pode logar um aviso ou retornar null/objeto vazio para 'prestador'.
                response.put("prestador", null); // Ou new HashMap<>()
                // logger.warn("Usuário {} é prestador mas não possui entidade Prestador
                // associada.", usuario.getId());
            }
        } else {
            // Se o usuário não for prestador, garantir que a chave 'prestador' não exista
            // ou seja nula
            response.put("prestador", null);
        }

        // Lógica para Cliente (se necessário, implementar de forma similar)
        // if (usuario.isCliente()) { ... }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/editar/{id}")
public ResponseEntity<?> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto usuarioDto,
        @RequestHeader("Authorization") String token) {
    if (token == null || !token.startsWith("Bearer ")) {
        throw new CustomException(ErrorMessages.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN_CODE);
    }

    String jwtToken = token.substring(7);
    String username = jwtUtil.extractUsername(jwtToken);

    if (!jwtUtil.validateToken(jwtToken, username)) {
        throw new CustomException(ErrorMessages.EXPIRED_TOKEN, ErrorMessages.EXPIRED_TOKEN_CODE);
    }

    // Check if the authenticated user is editing their own profile
    Usuario usuarioLogado = getUsuarioLogado(token);
    if (!usuarioLogado.getId().equals(id)) {
        throw new CustomException("Você não tem permissão para editar este usuário.", "USER_NOT_AUTHORIZED");
    }

    try {
        Optional<Usuario> usuarioEditado = usuarioService.editarUsuario(id, usuarioDto);
        if (usuarioEditado.isEmpty()) {
            throw new CustomException(ErrorMessages.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND_CODE);
        }
        return ResponseEntity.ok(new SuccessMessage(
                "Usuário editado com sucesso: " + usuarioEditado.get().getNome(), "USER_SUCCESS_003"));
    } catch (IllegalArgumentException e) {
        if (e.getMessage().contains("e-mail já existe")) {
            throw new CustomException(ErrorMessages.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS_CODE);
        } else if (e.getMessage().contains("e-mail inválido")) {
            throw new CustomException(ErrorMessages.INVALID_EMAIL, ErrorMessages.INVALID_EMAIL_CODE);
        }
        throw new CustomException("Erro ao editar usuário: " + e.getMessage(), "USER_ERROR_007");
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

        // Endpoint público para ver o perfil de OUTRO usuário (ex: cliente ver perfil de prestador, prestador ver de cliente)
    // O PerfilCliente.vue e PerfilPrestador.vue podem usar este.
    @GetMapping("/{id}") // Simplificado de /perfil-publico
    public ResponseEntity<UsuarioDto> getPerfilPublicoUsuario(@PathVariable Long id) {
        try {
            // buscarPerfilCompletoDto pode expor dados como email.
            // Para um perfil verdadeiramente público, você pode querer um DTO mais restrito.
            // Por ora, para a apresentação, vamos usar o mesmo.
            UsuarioDto perfilDto = usuarioService.buscarPerfilCompletoDto(id);
            return ResponseEntity.ok(perfilDto);
        } catch (IllegalArgumentException e) { // Usuário não encontrado
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}