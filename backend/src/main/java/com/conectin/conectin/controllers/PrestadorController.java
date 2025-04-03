package com.conectin.conectin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.entities.Especialidade;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.repository.EspecialidadeRepository;
import com.conectin.conectin.repository.PrestadorRepository;

@RestController
@RequestMapping("/api")
public class PrestadorController {
    @Autowired
    private PrestadorRepository prestadorRepository;
    @Autowired
    private EspecialidadeRepository especialidadeRepository;

    // Lista todas as especialidades disponíveis
    @GetMapping("/especialidades")
    public List<Especialidade> getEspecialidades() {
        return especialidadeRepository.findAll();
    }

    // Cria um novo prestador
    @PostMapping("/prestadores")
    public Prestador createPrestador(@RequestBody Prestador prestador) {
        return prestadorRepository.save(prestador);
    }

    // Outros endpoints conforme necessário
}
