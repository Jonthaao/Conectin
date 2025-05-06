package com.conectin.conectin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conectin.conectin.dto.PortfolioDto;
import com.conectin.conectin.entities.Portfolio;
import com.conectin.conectin.services.PortfolioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/adicionar")
    public ResponseEntity<Portfolio> adicionarPortfolio(@Valid @RequestBody PortfolioDto dto) {
        try {
            Portfolio portfolio = portfolioService.adicionarPortfolio(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(portfolio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}