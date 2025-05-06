package com.conectin.conectin.repository;

import com.conectin.conectin.entities.CidadePrestador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CidadePrestadorRepository extends JpaRepository<CidadePrestador, Integer> {
    List<CidadePrestador> findByPrestadorId(Integer prestadorId);
}