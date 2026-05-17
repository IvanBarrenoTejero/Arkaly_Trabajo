package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.ActivosFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivoFavoritoRepository extends JpaRepository<ActivosFavorito, Integer> {

    // Devuelve todos los favoritos de un usuario
    List<ActivosFavorito> findByIdUsuarioId(Integer idUsuario);
}