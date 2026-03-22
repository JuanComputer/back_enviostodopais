package com.sanchez.Envios.Dto;

import lombok.Data;

@Data
public class CambioEstadoDto {
    private String nuevoEstado;
    private String nota;  // Opcional, requerido en "No entregado"
}
