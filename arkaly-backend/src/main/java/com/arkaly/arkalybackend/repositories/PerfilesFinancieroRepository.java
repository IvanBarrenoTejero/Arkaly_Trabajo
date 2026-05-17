package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.PerfilesFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para el perfil financiero del usuario.
 * Cada usuario tiene como máximo un perfil financiero.
 */
@Repository
public interface PerfilesFinancieroRepository extends JpaRepository<PerfilesFinanciero, Integer> {

    // Devuelve el perfil financiero de un usuario si existe
    Optional<PerfilesFinanciero> findByIdUsuarioId(Integer idUsuario);
}