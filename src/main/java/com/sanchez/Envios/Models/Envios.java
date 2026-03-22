package com.sanchez.Envios.Models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "tb_envios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envios {

    @Id
    @GeneratedValue
    @Column(name = "tb_envios_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_envios_codigo_tracking", unique = true, nullable = false)
    private String codigoTracking;

    @Column(name = "tb_envios_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_envios_fecha_estimada")
    private LocalDate fechaEstimada;

    // Estado del envío con flujo definido
    @Column(name = "tb_envios_estado", nullable = false)
    private String estado;

    // Nota del último cambio de estado
    @Column(name = "tb_envios_nota_estado", length = 500)
    private String notaEstado;

    // Sede de origen (automática, sede del operador)
    @ManyToOne
    @JoinColumn(name = "tb_envios_origen_tienda_id")
    private Tiendas origen;

    // Sede de destino
    @ManyToOne
    @JoinColumn(name = "tb_envios_destino_id")
    private Tiendas destino;

    // Usuario que registró el envío
    @ManyToOne
    @JoinColumn(name = "tb_envios_registrado_por_id")
    private Usuarios registradoPor;

    // Emisor registrado (opcional)
    @ManyToOne
    @JoinColumn(name = "tb_envios_emisor_id")
    private Usuarios emisor;

    // Datos del emisor (registrado o anónimo)
    @Column(name = "tb_envios_emisor_nombre")
    private String emisorNombre;

    @Column(name = "tb_envios_emisor_dni", length = 11)
    private String emisorDni;

    @Column(name = "tb_envios_emisor_razon_social")
    private String emisorRazonSocial;

    @Column(name = "tb_envios_emisor_telefono")
    private String emisorTelefono;

    @Column(name = "tb_envios_emisor_correo")
    private String emisorCorreo;

    // Datos del receptor
    @Column(name = "tb_envios_receptor_nombre")
    private String receptorNombre;

    @Column(name = "tb_envios_receptor_dni", length = 11)
    private String receptorDni;

    @Column(name = "tb_envios_receptor_razon_social")
    private String receptorRazonSocial;

    // Entrega
    @Column(name = "tb_envios_tipo_entrega")
    private String tipoEntrega; // SEDE o DOMICILIO

    @Column(name = "tb_envios_direccion_entrega")
    private String direccionEntrega;

    @Column(name = "tb_envios_referencia_entrega")
    private String referenciaEntrega;

    // Paquete
    @Column(name = "tb_envios_peso", precision = 8, scale = 2)
    private BigDecimal peso;

    @Column(name = "tb_envios_valor_declarado", precision = 10, scale = 2)
    private BigDecimal valorDeclarado;

    @Column(name = "tb_envios_descripcion_paquete", length = 500)
    private String descripcionPaquete;

    @Column(name = "tb_envios_tipo_servicio")
    private String tipoServicio; // Estandar o Express

    // Documento (Boleta / Factura)
    @Column(name = "tb_envios_tipo_documento")
    private String tipoDocumento;

    @Column(name = "tb_envios_numero_documento")
    private String numeroDocumento; // B001-20250322-00000001

    @Column(name = "tb_envios_precio_envio", precision = 10, scale = 2)
    private BigDecimal precioEnvio;

    // Auditoría
    @Column(name = "tb_envios_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_envios_usuario_actualizacion")
    private UUID usuarioActualizacion;
}
