package com.conectin.conectin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Prestador;

public interface PrestadorRepository extends JpaRepository<Prestador, Integer> {
    List<Prestador> findAllByOrderByAvaliacaoMediaDesc();
    Optional<Prestador> findByUsuarioId(Long usuarioId);
}

    