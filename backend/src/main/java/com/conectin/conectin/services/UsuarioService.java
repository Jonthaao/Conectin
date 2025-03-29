package com.conectin.conectin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.UsuarioDto;
import com.conectin.conectin.entities.Cliente;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.TipoUsuario;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario cadastrarUsuario(UsuarioDto usuarioDto) {
        if (!usuarioDto.getSenha().equals(usuarioDto.getConfirmarSenha())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setEndereco(usuarioDto.getEndereco());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setSenha(usuarioDto.getSenha());

        // Verificar se o usuário é prestador
        if (TipoUsuario.PRESTADOR.equals(usuarioDto.getTipoUsuario())) {
            Prestador prestador = new Prestador();
            prestador.setUsuario(usuario);
            prestador.setServicosOferecidos("Serviços iniciais"); // Preencha conforme necessário
            prestador.setPortfolio("Portfólio inicial");
            usuario.setPrestador(prestador);
        }

        // Verificar se o usuário é cliente
        if (TipoUsuario.PRESTADOR.equals(usuarioDto.getTipoUsuario())) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            cliente.setHistoricoContratacoes("Histórico inicial"); // Preencha conforme necessário
            usuario.setCliente(cliente);
        }

        return usuarioRepository.save(usuario);
    }
    
}
