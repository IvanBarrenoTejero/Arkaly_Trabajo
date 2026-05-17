package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.MetasAhorro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaAhorroRepository extends JpaRepository<MetasAhorro, Integer> {

    // Todas las metas de un usuario, de más reciente a más antigua
    List<MetasAhorro> findByIdUsuarioIdOrderByFechaCreacionDesc(Integer idUsuario);

    // Solo las metas que todavía no se han completado
    List<MetasAhorro> findByIdUsuarioIdAndCompletadaFalse(Integer idUsuario);
}