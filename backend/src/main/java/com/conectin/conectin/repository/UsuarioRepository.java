package com.conectin.conectin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conectin.conectin.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByEmail(String email);
    Optional<Usuario> findById(Long id);
    boolean existsById(Long id);
    void deleteById(Long id);
    Optional<Usuario> findByEmail(String email);
}