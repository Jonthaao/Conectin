package com.conectin.conectin.services;

import com.conectin.conectin.entities.Cidade;
import com.conectin.conectin.repository.CidadeRepository;
import com.conectin.conectin.repository.CidadePrestadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CidadeService {

    @Autowired
    private CidadeRepository cidadeRepository;

    @Autowired
    private CidadePrestadorRepository cidadePrestadorRepository;

    public List<Cidade> buscarTodasCidades() {
        return cidadeRepository.findAll();
    }

    public List<Cidade> buscarCidadesPorPrestador(Integer prestadorId) {
        return cidadePrestadorRepository.findByPrestadorId(prestadorId)
                .stream()
                .map(cidadePrestador -> cidadePrestador.getCidade())
                .toList();
    }
}