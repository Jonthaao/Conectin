package com.conectin.conectin.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conectin.conectin.dto.PortfolioDto;
import com.conectin.conectin.entities.Portfolio;
import com.conectin.conectin.entities.Prestador;
import com.conectin.conectin.repository.PortfolioRepository;
import com.conectin.conectin.repository.PrestadorRepository;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    public Portfolio adicionarPortfolio(PortfolioDto dto) {
        Prestador prestador = prestadorRepository.findById(dto.getPrestadorId())
                .orElseThrow(() -> new IllegalArgumentException("Prestador não encontrado"));

        Portfolio portfolio = new Portfolio();
        portfolio.setPrestador(prestador);
        portfolio.setTitulo(dto.getTitulo());
        portfolio.setDescricao(dto.getDescricao());
        portfolio.setFotos(dto.getFotos());
        portfolio.setData(LocalDateTime.now());

        return portfolioRepository.save(portfolio);
    }
}
