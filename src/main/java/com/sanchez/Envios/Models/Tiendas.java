package com.sanchez.Envios.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_tiendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tiendas {

    @Id
    @GeneratedValue
    @Column(name = "tb_tiendas_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_tiendas_nombre", nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "tb_ubicacion_distritos_id")
    private UbicacionDistritos distrito;

    @Column(name = "tb_tiendas_direccion", nullable = false)
    private String direccion;

    @Column(name = "tb_tiendas_latitud", precision = 10, scale = 6)
    private BigDecimal latitud;

    @Column(name = "tb_tiendas_longitud", precision = 10, scale = 6)
    private BigDecimal longitud;

    @Column(name = "tb_tiendas_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_tiendas_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_tiendas_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_tiendas_usuario_actualizacion")
    private UUID usuarioActualizacion;
}