package com.conectin.conectin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
}