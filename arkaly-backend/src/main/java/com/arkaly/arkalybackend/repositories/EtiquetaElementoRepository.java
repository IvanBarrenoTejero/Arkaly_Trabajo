package com.arkaly.arkalybackend.repositories;

import com.arkaly.arkalybackend.models.EtiquetaElemento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la relación entre etiquetas y elementos (carpetas o documentos).
 * Un elemento puede tener una etiqueta, y una etiqueta puede estar en varios elementos.
 */
@Repository
public interface EtiquetaElementoRepository extends JpaRepository<EtiquetaElemento, Integer> {

    // Etiqueta asignada a una carpeta concreta
    Optional<EtiquetaElemento> findByIdCarpetaId(Integer idCarpeta);

    // Etiqueta asignada a un documento concreto
    Optional<EtiquetaElemento> findByIdDocumentoId(Integer idDocumento);

    // Todos los elementos que tienen una etiqueta concreta
    List<EtiquetaElemento> findByIdEtiquetaId(Integer idEtiqueta);
}