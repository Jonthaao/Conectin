package com.conectin.conectin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}