package com.sanchez.Envios.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EnvioRequestDto {
    private UUID emisorId;
    private String emisorNombre;
    private String emisorDni;
    private String emisorTelefono;
    private String emisorCorreo;
    private UUID destinoId;
    private String receptorNombre;
    private String receptorDni;
    private LocalDate fechaEstimada;

    // Campos opcionales si luego agregamos entrega a domicilio:
    private String tipoEntrega; // "SEDE" o "DOMICILIO"
    private String direccionEntrega;
    private String referenciaEntrega;
}
