package com.sanchez.Envios.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EnvioRequestDto {
    private UUID emisorId;
    private String emisorNombre;
    private String emisorDni;           // DNI (8 dígitos) o RUC (11 dígitos)
    private String emisorRazonSocial;   // Nombre de empresa si tiene RUC
    private String emisorTelefono;
    private String emisorCorreo;
    private UUID destinoId;
    private String receptorNombre;
    private String receptorDni;         // DNI (8 dígitos) o RUC (11 dígitos)
    private String receptorRazonSocial; // Nombre de empresa si el receptor tiene RUC
    private LocalDate fechaEstimada;

    // Entrega
    private String tipoEntrega;         // "SEDE" o "DOMICILIO"
    private String direccionEntrega;
    private String referenciaEntrega;

    // Boleta / Factura
    private String tipoDocumento;       // "BOLETA" o "FACTURA"
    private String descripcionPaquete;  // Detalle del contenido
    private BigDecimal precioEnvio;     // Precio acordado del envío
}
