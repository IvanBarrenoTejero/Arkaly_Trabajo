package com.arkaly.arkalybackend.dto;

import lombok.Data;

// DTO para las etiquetas que el usuario puede asignar a carpetas y documentos
@Data
public class EtiquetaDTO {
    private Integer id;
    private String nombre;
    private String color;
}