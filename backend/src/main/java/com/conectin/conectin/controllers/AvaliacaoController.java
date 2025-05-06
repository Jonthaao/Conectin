package com.conectin.conectin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.conectin.conectin.dto.AvaliacaoDto;
import com.conectin.conectin.entities.Avaliacao;
import com.conectin.conectin.repository.AvaliacaoRepository;
import com.conectin.conectin.services.AvaliacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @PostMapping("/criar")
    public ResponseEntity<Avaliacao> criarAvaliacao(@Valid @RequestBody AvaliacaoDto dto) {
        try {
            Avaliacao avaliacao = avaliacaoService.criarAvaliacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Avaliacao>> listarAvaliacoesPorUsuario(@PathVariable Integer usuarioId) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByAvaliadoId(usuarioId);
        return ResponseEntity.ok(avaliacoes);
    }
}