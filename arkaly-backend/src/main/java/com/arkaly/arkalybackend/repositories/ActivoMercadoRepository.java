package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.ActivosMercado;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para los activos disponibles en el mercado (acciones, ETFs, crypto...).
 */
@Repository
public interface ActivoMercadoRepository extends JpaRepository<ActivosMercado, Integer> {

    // Activos filtrados por tipo (ACCION, ETF, CRYPTO...)
    List<ActivosMercado> findByTipoActivo(TipoActivo tipoActivo);

    // Busca un activo por símbolo ignorando mayúsculas/minúsculas (ej: "aapl" == "AAPL")
    Optional<ActivosMercado> findBySimboloIgnoreCase(String simbolo);
}