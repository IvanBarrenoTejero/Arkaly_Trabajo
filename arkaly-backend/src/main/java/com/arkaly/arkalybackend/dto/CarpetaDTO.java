package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

// DTO para las carpetas del gestor de documentos. Soporta estructura jerárquica
@Data
public class CarpetaDTO {
    private Integer id;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private Integer idUsuario;
    private Integer idCarpetaPadre;  // null si es carpeta raíz
    private EtiquetaDTO etiqueta;
}