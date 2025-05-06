package com.conectin.conectin.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.PrestadorDto;
import com.conectin.conectin.entities.CidadePrestador;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.entities.PrestadorCategoria;
import com.conectin.conectin.entities.Usuario;
import com.conectin.conectin.repository.CidadePrestadorRepository;
import com.conectin.conectin.repository.PrestadorCategoriaRepository;
import com.conectin.conectin.repository.PrestadorRepository;
import com.conectin.conectin.repository.UsuarioRepository;

@Service
public class PrestadorService {

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestadorCategoriaRepository prestadorCategoriaRepository;

    @Autowired
    private CidadePrestadorRepository cidadePrestadorRepository;

    public Prestador criarPrestador(PrestadorDto dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Prestador prestador = new Prestador();
        prestador.setUsuario(usuario);
        prestador.setDescricao(dto.getDescricao());
        prestador.setDisponibilidade(dto.getDisponibilidade());
        prestador.setAvaliacaoMedia(dto.getAvaliacaoMedia() != null ? dto.getAvaliacaoMedia() : 0.0f);

        return prestadorRepository.save(prestador);
    }

    public List<Prestador> findPrestadoresPorCategoriaECidade(Integer categoriaId, Integer cidadeId) {
        // Busca prestadores que atuam na categoria
        List<PrestadorCategoria> prestadorCategorias = prestadorCategoriaRepository.findAll().stream()
                .filter(tc -> tc.getCategoria().getId().equals(categoriaId))
                .collect(Collectors.toList());

        List<Prestador> prestadoresPorCategoria = prestadorCategorias.stream()
                .map(PrestadorCategoria::getPrestador)
                .distinct()
                .collect(Collectors.toList());

        // Busca prestadores que atuam na cidade
        List<CidadePrestador> cidadePrestadores = cidadePrestadorRepository.findAll().stream()
                .filter(ct -> ct.getCidade().getId().equals(cidadeId))
                .collect(Collectors.toList());

        List<Prestador> prestadoresPorCidade = cidadePrestadores.stream()
                .map(CidadePrestador::getPrestador)
                .distinct()
                .collect(Collectors.toList());

        // Interseção: prestadores que estão na categoria E na cidade
        return prestadoresPorCategoria.stream()
                .filter(prestadoresPorCidade::contains)
                .sorted((t1, t2) -> t2.getAvaliacaoMedia().compareTo(t1.getAvaliacaoMedia())) // Ordena por avaliação
                .collect(Collectors.toList());
    }
}