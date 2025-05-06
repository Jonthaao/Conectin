package com.conectin.conectin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.dto.SolicitacaoServicoDto;
import com.conectin.conectin.entities.SolicitacaoServico;
import com.conectin.conectin.services.SolicitacaoServicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoServicoController {

    @Autowired
    private SolicitacaoServicoService solicitacaoServicoService;

    @PostMapping("/criar")
    public ResponseEntity<SolicitacaoServico> criarSolicitacao(@Valid @RequestBody SolicitacaoServicoDto dto) {
        try {
            SolicitacaoServico solicitacao = solicitacaoServicoService.criarSolicitacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}