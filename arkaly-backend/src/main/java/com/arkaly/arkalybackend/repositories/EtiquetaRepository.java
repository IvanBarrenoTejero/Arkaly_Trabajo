package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {
    List<Etiqueta> findByIdUsuarioId(Integer idUsuario);
}