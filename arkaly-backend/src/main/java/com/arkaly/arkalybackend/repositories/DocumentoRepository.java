package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Integer> {
    // Documentos dentro de una carpeta concreta
    List<Documento> findByIdCarpetaId(Integer idCarpeta);

    // Documentos sin carpeta asignada (raíz)
    List<Documento> findByIdUsuarioIdAndIdCarpetaIsNull(Integer idUsuario);

    // Buscar documentos por nombre
    List<Documento> findByIdUsuarioIdAndNombreOriginalContainingIgnoreCase(
            Integer idUsuario, String nombre);

    // Todos los documentos de un usuario
    List<Documento> findByIdUsuarioId(Integer idUsuario);

    // Documentos por categoría
    List<Documento> findByIdUsuarioIdAndCategoria(
            Integer idUsuario, String categoria);

    // Comprobar si existe un archivo con ese nombre interno
    boolean existsByNombreArchivo(String nombreArchivo);
}