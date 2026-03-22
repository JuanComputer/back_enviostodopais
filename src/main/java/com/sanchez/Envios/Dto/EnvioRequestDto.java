package com.sanchez.Envios.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EnvioRequestDto {
    // Emisor (anónimo o registrado)
    private UUID emisorId;              // Si está registrado en el sistema
    private String emisorNombre;        // Nombre (persona natural)
    private String emisorRazonSocial;   // Razón social (empresa con RUC)
    private String emisorDni;           // DNI (8) o RUC (11)
    private String emisorTelefono;
    private String emisorCorreo;

    // Receptor
    private String receptorNombre;
    private String receptorRazonSocial;
    private String receptorDni;         // DNI (8) o RUC (11)

    // Entrega
    private UUID destinoId;             // Sede destino
    private String tipoEntrega;         // SEDE o DOMICILIO
    private String direccionEntrega;
    private String referenciaEntrega;

    // Paquete
    private BigDecimal peso;            // kg, obligatorio
    private BigDecimal valorDeclarado;  // S/., obligatorio
    private String descripcionPaquete;
    private String tipoServicio;        // Estandar o Express

    // Documento
    private String tipoDocumento;       // BOLETA o FACTURA

    // Fecha estimada (opcional, si null se calcula automáticamente)
    private LocalDate fechaEstimada;
}
