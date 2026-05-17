package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Búsqueda por email, usada en el login y en la validación JWT
    Optional<Usuario> findByEmail(String email);

    // Búsqueda por nombre de usuario
    Optional<Usuario> findByUsername(String username);

    // Comprobaciones de unicidad antes de registrar un usuario nuevo
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}