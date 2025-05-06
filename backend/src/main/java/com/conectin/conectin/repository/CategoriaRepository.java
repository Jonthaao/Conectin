package com.conectin.conectin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}