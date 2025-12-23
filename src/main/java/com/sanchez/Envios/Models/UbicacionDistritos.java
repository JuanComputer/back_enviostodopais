package com.sanchez.Envios.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_ubicacion_distritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionDistritos {

    @Id
    @GeneratedValue
    @Column(name = "tb_ubicacion_distritos_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_ubicacion_distritos_nombre", nullable = false)
    private String nombre;

    @Column(name = "tb_ubicacion_distritos_codigo")
    private String codigo;

    @Column(name = "tb_ubicacion_provincias_codigo")
    private String provinciaCodigo;

    @ManyToOne
    @JoinColumn(name = "tb_ubicacion_provincias_id")
    private UbicacionProvincias provincia;

    @Column(name = "tb_ubicacion_distritos_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_ubicacion_distritos_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_ubicacion_distritos_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_ubicacion_distritos_usuario_actualizacion")
    private UUID usuarioActualizacion;
}
