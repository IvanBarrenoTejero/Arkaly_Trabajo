package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Posicione;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para las posiciones abiertas del usuario en cada activo.
 * Una posición representa el estado actual de un activo en cartera.
 */
@Repository
public interface PosicionRepository extends JpaRepository<Posicione, Integer> {

    // Todas las posiciones abiertas de un usuario
    List<Posicione> findByIdUsuarioId(Integer idUsuario);

    // Posiciones filtradas por tipo de activo (ACCION, ETF, CRYPTO...)
    List<Posicione> findByIdUsuarioIdAndTipoActivo(Integer idUsuario, TipoActivo tipoActivo);

    // Posición concreta de un usuario en un símbolo determinado
    Optional<Posicione> findByIdUsuarioIdAndSimbolo(Integer idUsuario, String simbolo);
}