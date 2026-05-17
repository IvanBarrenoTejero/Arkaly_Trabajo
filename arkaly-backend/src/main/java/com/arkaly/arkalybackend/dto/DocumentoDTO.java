package com.arkaly.arkalybackend.dto;

import lombok.Data;

// DTO de entrada: datos que envía el frontend al subir un documento
@Data
public class DocumentoDTO {
    private Integer id;
    private String nombreOriginal;
    private String tipoMime;
    private Long tamanioBytes;
    private String categoria;
    private Integer idUsuario;
    private Integer idCarpeta;  // null si se sube a la raíz
}