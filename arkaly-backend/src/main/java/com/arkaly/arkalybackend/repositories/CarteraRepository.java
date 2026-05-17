package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Cartera;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarteraRepository extends JpaRepository<Cartera, Integer> {

    // Primera compra registrada de un símbolo para un usuario (para calcular precio medio)
    Optional<Cartera> findFirstByIdUsuarioIdAndSimboloOrderByFechaOperacionAsc(
            Integer idUsuario, String simbolo);

    // Todos los movimientos de un usuario, del más reciente al más antiguo
    List<Cartera> findByIdUsuarioIdOrderByFechaOperacionDesc(Integer idUsuario);

    // Movimientos filtrados por tipo de activo (ACCION, ETF, CRYPTO...)
    List<Cartera> findByIdUsuarioIdAndTipoActivoOrderByFechaOperacionDesc(
            Integer idUsuario, TipoActivo tipoActivo);
}