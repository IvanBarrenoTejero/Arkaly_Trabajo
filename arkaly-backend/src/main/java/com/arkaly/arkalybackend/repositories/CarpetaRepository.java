package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Carpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarpetaRepository extends JpaRepository<Carpeta, Integer> {

    // Todas las carpetas raíz de un usuario (sin carpeta padre)
    List<Carpeta> findByIdUsuarioIdAndIdCarpetaPadreIsNull(Integer idUsuario);

    // Subcarpetas de una carpeta concreta
    List<Carpeta> findByIdCarpetaPadreId(Integer idCarpetaPadre);

    // Todas las carpetas de un usuario
    List<Carpeta> findByIdUsuarioId(Integer idUsuario);

    // Comprobar si ya existe una carpeta con ese nombre en el mismo nivel
    boolean existsByNombreAndIdUsuarioIdAndIdCarpetaPadreId(
            String nombre, Integer idUsuario, Integer idCarpetaPadre);

    // Búsqueda de carpetas por nombre parcial, sin distinguir mayúsculas
    List<Carpeta> findByIdUsuarioIdAndNombreContainingIgnoreCase(
            Integer idUsuario, String nombre);
}