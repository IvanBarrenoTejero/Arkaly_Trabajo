package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.AlertasPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaPrecioRepository extends JpaRepository<AlertasPrecio, Integer> {
    List<AlertasPrecio> findByIdUsuarioIdOrderByFechaCreacionDesc(Integer idUsuario);
    List<AlertasPrecio> findByIdUsuarioIdAndActivaTrue(Integer idUsuario);
}