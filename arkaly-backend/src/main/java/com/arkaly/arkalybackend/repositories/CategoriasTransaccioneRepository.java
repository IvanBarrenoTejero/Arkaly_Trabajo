package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.CategoriasTransaccione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriasTransaccioneRepository extends JpaRepository<CategoriasTransaccione, Integer> {
    List<CategoriasTransaccione> findByIdUsuarioId(Integer idUsuario);
}