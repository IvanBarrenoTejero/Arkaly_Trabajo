package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

// DTO de salida: datos que se devuelven al frontend tras subir o consultar un documento
@Data
public class DocumentoResponseDTO {
    private Integer id;
    private String nombreOriginal;
    private String rutaRelativa;
    private String tipoMime;
    private Long tamanioBytes;
    private String categoria;
    private LocalDateTime fechaSubida;
    private Integer idUsuario;
    private Integer idCarpeta;
    private EtiquetaDTO etiqueta;
}