package com.conectin.conectin.controllers;

import com.conectin.conectin.entities.Cidade;
import com.conectin.conectin.services.CidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cidades")
public class CidadeController {

    @Autowired
    private CidadeService cidadeService;

    @GetMapping
    public ResponseEntity<List<Cidade>> buscarTodasCidades() {
        List<Cidade> cidades = cidadeService.buscarTodasCidades();
        return ResponseEntity.ok(cidades);
    }

    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<List<Cidade>> buscarCidadesPorPrestador(@PathVariable Integer prestadorId) {
        List<Cidade> cidades = cidadeService.buscarCidadesPorPrestador(prestadorId);
        return ResponseEntity.ok(cidades);
    }
}