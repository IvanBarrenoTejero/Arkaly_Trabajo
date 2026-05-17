package com.arkaly.arkalybackend.dto;

import lombok.Data;

// DTO para las categorías de transacciones (nombre, color e icono para el frontend)
@Data
public class CategoriaTransaccionDTO {
    private Integer id;
    private String nombre;
    private String color;
    private String icono;
}