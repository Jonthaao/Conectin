package com.conectin.conectin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Cidade;

public interface CidadeRepository extends JpaRepository<Cidade, Integer> {
}