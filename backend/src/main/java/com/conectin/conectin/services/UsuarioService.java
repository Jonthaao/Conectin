package com.conectin.conectin.services;

import com.conectin.conectin.dto.CategoriaDto;
import com.conectin.conectin.dto.CidadeDto;
import com.conectin.conectin.dto.PortfolioDto;
import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.*; // Import all entities
import com.conectin.conectin.repository.*; // Import all repositories
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt; // Mantenha seu BCrypt ou use PasswordEncoder do Spring Security
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet; // Para as coleções em Prestador
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CidadeRepository cidadeRepository; // Já estava

    @Autowired
    private CategoriaRepository categoriaRepository; // ADICIONADO

    @Autowired
    private PrestadorCategoriaRepository prestadorCategoriaRepository; // ADICIONADO

    @Autowired
    private CidadePrestadorRepository cidadePrestadorRepository; // Já estava

    // Se for gerenciar Portfolios de forma mais complexa:
    // @Autowired
    // private PortfolioRepository portfolioRepository;


    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Transactional // Adicionado para o cadastro também ser transacional
    public Usuario cadastrarUsuario(UsuarioDto usuarioDto) {
        if (!EMAIL_PATTERN.matcher(usuarioDto.getEmail()).matches()) {
            throw new IllegalArgumentException("Formato de e-mail inválido");
        }

        if (usuarioRepository.findByEmail(usuarioDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Este e-mail já existe");
        }

        if (usuarioDto.getSenha() == null || usuarioDto.getSenha().isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória para cadastro.");
        }
        if (!usuarioDto.isSenhasCoincidem()) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setTelefone(usuarioDto.getTelefone());
        usuario.setSenha(BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt())); // Considere usar BCryptPasswordEncoder do Spring Security
        usuario.setFotoPerfil(usuarioDto.getFotoPerfil());

        if (usuarioDto.isPrestador()) {
            usuario.addTipo(TipoUsuario.PRESTADOR);
        }
        if (usuarioDto.isCliente()) {
            usuario.addTipo(TipoUsuario.CLIENTE);
        }

        if (usuario.getTipos().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um tipo de usuário deve ser selecionado");
        }

        // Salva o usuário primeiro para obter o ID, se necessário para referências
        // Mas como Prestador tem cascade, pode ser salvo junto.
        // usuario = usuarioRepository.save(usuario); // Pode ser omitido se o cascade do Usuario->Prestador for suficiente

        if (usuario.isPrestador()) {
            Prestador prestador = new Prestador();
            prestador.setUsuario(usuario); // Associa ao usuário
            usuario.setPrestador(prestador); // Associação bidirecional

            prestador.setDescricao(usuarioDto.getDescricao() != null ? usuarioDto.getDescricao() : "");
            prestador.setDisponibilidade(usuarioDto.getDisponibilidade() != null ? usuarioDto.getDisponibilidade() : "");
            prestador.setAvaliacaoMedia(0.0f);

            // Lógica de cadastro inicial de categorias e cidades para prestador (se houver no DTO de cadastro)
            // Se o UsuarioDto tiver categorias e cidades no cadastro:
            if (usuarioDto.getCategorias() != null) {
                for (CategoriaDto catDto : usuarioDto.getCategorias()) {
                    // Assumindo que Categoria tem ID Integer
                    Categoria categoria = categoriaRepository.findById(catDto.getId().intValue())
                            .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada no cadastro: ID " + catDto.getId()));
                    PrestadorCategoria pc = new PrestadorCategoria();
                    pc.setPrestador(prestador);
                    pc.setCategoria(categoria);
                    prestador.getPrestadorCategorias().add(pc); // Adiciona à lista do prestador
                }
            }
            if (usuarioDto.getCidades() != null) {
                 for (CidadeDto cidDto : usuarioDto.getCidades()) {
                    // Assumindo que Cidade tem ID Integer
                    Cidade cidade = cidadeRepository.findById(cidDto.getId().intValue())
                            .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada no cadastro: ID " + cidDto.getId()));
                    CidadePrestador cp = new CidadePrestador();
                    cp.setPrestador(prestador);
                    cp.setCidade(cidade);
                    prestador.getCidadePrestadores().add(cp); // Adiciona à lista do prestador
                }
            }
            // Portfolios no cadastro
            if (usuarioDto.getPortfolios() != null && !usuarioDto.getPortfolios().isEmpty()) {
                for (PortfolioDto portfolioDto : usuarioDto.getPortfolios()) {
                    Portfolio portfolio = new Portfolio();
                    // portfolio.setTitulo(portfolioDto.getTitulo());
                    portfolio.setDescricao(portfolioDto.getDescricao());
                    // portfolio.setFotos(portfolioDto.getFotos()); // Se fotos forem strings (URLs, base64)
                    // Lógica para salvar imagens se forem arquivos
                    portfolio.setPrestador(prestador);
                    prestador.getPortfolios().add(portfolio);
                }
            }
            // O save do usuário com cascade=ALL no prestador vai salvar o prestador e suas coleções
        }

        if (usuario.isCliente()) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            usuario.setCliente(cliente); // Associação bidirecional
            cliente.setHistoricoContratacoes(""); // Ou um valor padrão
            // O save do usuário com cascade=ALL no cliente vai salvar o cliente
        }
        
        return usuarioRepository.save(usuario); // Salva o usuário e, por cascata, Prestador e Cliente e suas sub-entidades
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Usuario findById(Long id) { // Mantive o tipo Long como no seu método
        return usuarioRepository.findById(id).orElse(null);
    }

    @Transactional // MUITO IMPORTANTE AQUI!
    public Optional<Usuario> editarUsuario(Long id, UsuarioDto usuarioDto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        if (!EMAIL_PATTERN.matcher(usuarioDto.getEmail()).matches()) {
            throw new IllegalArgumentException("Formato de e-mail inválido");
        }

        Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmail(usuarioDto.getEmail());
        if (usuarioByEmail.isPresent() && usuarioByEmail.get().getId() != id.longValue()) { // Corrigido: !equals(id)
            throw new IllegalArgumentException("Este e-mail já está em uso por outra conta.");
        }

        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());
        if (usuarioDto.getFotoPerfil() != null) { // Só atualiza se não for nulo
             usuario.setFotoPerfil(usuarioDto.getFotoPerfil());
        }


        // Lógica de senha
        if (usuarioDto.getSenhaAtual() != null && !usuarioDto.getSenhaAtual().isEmpty()) {
            if (!BCrypt.checkpw(usuarioDto.getSenhaAtual(), usuario.getSenha())) {
                 throw new IllegalArgumentException("Senha atual incorreta.");
            }
            if (usuarioDto.getSenha() == null || usuarioDto.getSenha().isEmpty()) {
                throw new IllegalArgumentException("Nova senha não pode ser vazia.");
            }
            if (!usuarioDto.isSenhasCoincidem()) {
                throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
            }
            usuario.setSenha(BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt()));
        }


        // Atualizar tipos e entidades associadas (Prestador/Cliente)
        boolean eraPrestador = usuario.isPrestador();
        boolean eraCliente = usuario.isCliente();

        usuario.getTipos().clear(); // Limpa os tipos atuais
        if (usuarioDto.isPrestador()) {
            usuario.addTipo(TipoUsuario.PRESTADOR);
        }
        if (usuarioDto.isCliente()) {
            usuario.addTipo(TipoUsuario.CLIENTE);
        }

        if (usuario.getTipos().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um tipo de usuário deve ser selecionado (Prestador ou Cliente).");
        }

        // Gerenciamento do Prestador
        if (usuario.isPrestador()) {
            Prestador prestador = prestadorRepository.findByUsuarioId(usuario.getId())
                                     .orElseGet(() -> {
                                         Prestador novoPrestador = new Prestador();
                                         novoPrestador.setUsuario(usuario);
                                         usuario.setPrestador(novoPrestador); // Link bidirecional
                                         return novoPrestador;
                                     });

            prestador.setDescricao(usuarioDto.getDescricao());
            prestador.setDisponibilidade(usuarioDto.getDisponibilidade());

            // ---- GERENCIAR CATEGORIAS ----
            // 1. Limpar categorias antigas do prestador diretamente da coleção.
            //    Com orphanRemoval=true, o Hibernate cuidará de deletar da tabela de junção.
            prestador.getPrestadorCategorias().clear(); // Isso aciona o orphanRemoval
            // Se não usar orphanRemoval ou quiser ser explícito:
            // prestadorCategoriaRepository.deleteByPrestador(prestador); // Deleta do banco

            // 2. Adicionar novas categorias
            if (usuarioDto.getCategorias() != null) {
                for (CategoriaDto catDto : usuarioDto.getCategorias()) {
                    // Assumindo que Categoria.id é Integer e CategoriaDto.id é Long
                    Categoria categoria = categoriaRepository.findById(catDto.getId().intValue())
                            .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: ID " + catDto.getId()));
                    
                    PrestadorCategoria prestadorCategoria = new PrestadorCategoria();
                    prestadorCategoria.setPrestador(prestador);
                    prestadorCategoria.setCategoria(categoria);
                    prestador.getPrestadorCategorias().add(prestadorCategoria); // Adiciona à coleção do prestador
                }
            }

            // ---- GERENCIAR CIDADES ----
            // 1. Limpar cidades antigas
            prestador.getCidadePrestadores().clear(); // Aciona orphanRemoval
            // Ou: cidadePrestadorRepository.deleteByPrestador(prestador);

            // 2. Adicionar novas cidades
            if (usuarioDto.getCidades() != null) {
                for (CidadeDto cidDto : usuarioDto.getCidades()) {
                     // Assumindo que Cidade.id é Integer e CidadeDto.id é Long
                    Cidade cidade = cidadeRepository.findById(cidDto.getId().intValue())
                            .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada: ID " + cidDto.getId()));

                    CidadePrestador cidadePrestador = new CidadePrestador();
                    cidadePrestador.setPrestador(prestador);
                    cidadePrestador.setCidade(cidade);
                    prestador.getCidadePrestadores().add(cidadePrestador); // Adiciona à coleção do prestador
                }
            }

            // ---- GERENCIAR PORTFÓLIOS ----
            // Lógica de portfólio: pode ser mais complexa se envolver upload de arquivos.
            // Se for apenas texto e URLs de imagens:
            // 1. Limpar portfólios antigos (assumindo que são completamente substituídos)
            prestador.getPortfolios().clear(); // Aciona orphanRemoval para Portfolio
            // Ou: portfolioRepository.deleteByPrestador(prestador); // se Portfolio tiver referência ao Prestador

            // 2. Adicionar/atualizar portfólios
            if (usuarioDto.getPortfolios() != null && !usuarioDto.getPortfolios().isEmpty()) {
                for (PortfolioDto portfolioDto : usuarioDto.getPortfolios()) {
                    Portfolio portfolio = new Portfolio();
                    // Se portfolioDto tiver um ID e você quiser atualizar em vez de recriar:
                    // if (portfolioDto.getId() != null) {
                    //     portfolio = portfolioRepository.findById(portfolioDto.getId()).orElse(new Portfolio());
                    // }
                    // portfolio.setTitulo(portfolioDto.getTitulo()); // Se tiver título
                    portfolio.setDescricao(portfolioDto.getDescricao());
                    // portfolio.setUrlImagem(portfolioDto.getUrlImagem()); // Assumindo que Portfolio tem urlImagem
                    // Se Portfolios tem 'fotos' como List<String>
                    if(portfolioDto.getFotos() != null) {
                        portfolio.setFotos(new ArrayList<>(portfolioDto.getFotos()));
                    } else {
                        portfolio.setFotos(new ArrayList<>());
                    }

                    portfolio.setPrestador(prestador); // Link ao prestador
                    prestador.getPortfolios().add(portfolio);
                }
            }
            // O prestadorRepository.save(prestador) não é estritamente necessário aqui se
            // o usuário for salvo e tiver cascade para prestador. Mas pode ser bom para clareza.
            // prestadorRepository.save(prestador);

        } else if (eraPrestador) { // Se deixou de ser prestador, mas era antes
            prestadorRepository.findByUsuarioId(usuario.getId()).ifPresent(prestadorParaDeletar -> {
                usuario.setPrestador(null); // Remove a referência do usuário
                prestadorRepository.delete(prestadorParaDeletar); // Deleta o prestador e suas associações (devido a cascade e orphanRemoval)
            });
        }

        // Gerenciamento do Cliente
        if (usuario.isCliente()) {
            if (!eraCliente) { // Se tornou cliente agora
                Cliente cliente = new Cliente();
                cliente.setUsuario(usuario);
                usuario.setCliente(cliente); // Link bidirecional
                // cliente.setHistoricoContratacoes(""); // Definir valor padrão se necessário
                // clienteRepository.save(cliente); // Será salvo pela cascata do usuário
            }
            // Se já era cliente, não precisa fazer nada a menos que haja campos do cliente para atualizar no DTO
        } else if (eraCliente) { // Se deixou de ser cliente
            clienteRepository.findByUsuarioId(usuario.getId()).ifPresent(clienteParaDeletar -> {
                usuario.setCliente(null); // Remove a referência do usuário
                clienteRepository.delete(clienteParaDeletar);
            });
        }
        
        return Optional.of(usuarioRepository.save(usuario)); // Salva o usuário, e por cascata, o Prestador/Cliente e suas coleções
    }


    @Transactional
    public boolean deletarUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        // A deleção do usuário deve, por cascata, deletar Prestador, Cliente e suas sub-entidades
        // como PrestadorCategoria, CidadePrestador, Portfolio se configurado corretamente.
        usuarioRepository.deleteById(id);
        return true;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Método para buscar perfil (você já tinha, mas revisado para consistência)
    // Este método DEVE ser usado pelo seu UsuarioController no endpoint /perfil
    public UsuarioDto buscarPerfilCompletoDto(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId));

        UsuarioDto dto = new UsuarioDto();
        dto.setNome(usuario.getNome());
        dto.setEndereco(usuario.getEndereco());
        dto.setEmail(usuario.getEmail());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        dto.setPrestador(usuario.isPrestador());
        dto.setCliente(usuario.isCliente());

        if (usuario.isPrestador() && usuario.getPrestador() != null) { // Verifica se a entidade Prestador existe
            Prestador prestador = usuario.getPrestador();
            dto.setDescricao(prestador.getDescricao());
            dto.setDisponibilidade(prestador.getDisponibilidade());
            
            // Portfólios
            if (prestador.getPortfolios() != null) {
                dto.setPortfolios(
                    prestador.getPortfolios().stream()
                        .map(p -> convertToPortfolioDto(p, prestador.getId())) // Renomeei seu método
                        .collect(Collectors.toList())
                );
            } else {
                dto.setPortfolios(Collections.emptyList());
            }
        
            // Cidades
            if (prestador.getCidadePrestadores() != null) {
                dto.setCidades(prestador.getCidadePrestadores().stream()
                    .map(this::convertCidadePrestadorToCidadeDto) // Renomeei seu método
                    .collect(Collectors.toList()));
            } else {
                dto.setCidades(Collections.emptyList());
            }

            // Categorias
            if (prestador.getPrestadorCategorias() != null) {
                dto.setCategorias(prestador.getPrestadorCategorias().stream()
                    .map(this::convertPrestadorCategoriaToCategoriaDto) // Renomeei seu método
                    .collect(Collectors.toList()));
            } else {
                dto.setCategorias(Collections.emptyList());
            }
        } else { // Se não for prestador, ou se a entidade prestador for nula, inicializa listas vazias
            dto.setPortfolios(Collections.emptyList());
            dto.setCidades(Collections.emptyList());
            dto.setCategorias(Collections.emptyList());
        }

        return dto;
    }

    // Métodos conversores (seus métodos com nomes ligeiramente ajustados para clareza)
    private PortfolioDto convertToPortfolioDto(Portfolio p, Integer prestadorId) {
        PortfolioDto portfolioDto = new PortfolioDto();
        if (p.getId() != null) { // Adicionar ID do portfolio se existir e o DTO suportar
           // portfolioDto.setId(p.getId());
        }
        portfolioDto.setPrestadorId(prestadorId); // Pode não ser necessário no DTO de resposta do perfil
        // portfolioDto.setTitulo(p.getTitulo()); // Se Portfolio tiver título
        portfolioDto.setDescricao(p.getDescricao());
        portfolioDto.setFotos(p.getFotos() != null ? new ArrayList<>(p.getFotos()) : Collections.emptyList());
        // Se o frontend espera 'urlImagem' para preview
        // if (p.getFotos() != null && !p.getFotos().isEmpty()){
        //    portfolioDto.setUrlImagem(p.getFotos().get(0)); // Pega a primeira foto como preview
        // }
        return portfolioDto;
    }

    private CidadeDto convertCidadePrestadorToCidadeDto(CidadePrestador cp) {
        CidadeDto cidadeDto = new CidadeDto();
        // Assumindo que Cidade.id é Integer e CidadeDto.id é Long
        cidadeDto.setId(cp.getCidade().getId().longValue()); 
        cidadeDto.setNome(cp.getCidade().getNome());
        return cidadeDto;
    }

    private CategoriaDto convertPrestadorCategoriaToCategoriaDto(PrestadorCategoria pc) {
        CategoriaDto categoriaDto = new CategoriaDto();
        // Assumindo que Categoria.id é Integer e CategoriaDto.id é Long
        categoriaDto.setId(pc.getCategoria().getId().longValue());
        categoriaDto.setNome(pc.getCategoria().getNome());
        return categoriaDto;
    }
}