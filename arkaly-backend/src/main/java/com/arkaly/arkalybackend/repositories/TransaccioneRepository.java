package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Transaccione;
import com.arkaly.arkalybackend.models.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransaccioneRepository extends JpaRepository<Transaccione, Integer> {

    // Todas las transacciones de un usuario, ordenadas por fecha descendente
    List<Transaccione> findByIdUsuarioIdOrderByFechaDesc(Integer idUsuario);

    // Transacciones de un usuario en un rango de fechas, ordenadas por fecha descendente
    List<Transaccione> findByIdUsuarioIdAndFechaBetweenOrderByFechaDesc(
            Integer idUsuario, LocalDate desde, LocalDate hasta);

    // Transacciones de un usuario filtradas por tipo (ingreso/gasto) y rango de fechas
    List<Transaccione> findByIdUsuarioIdAndTipoAndFechaBetween(
            Integer idUsuario, TipoTransaccion tipo, LocalDate desde, LocalDate hasta);
}