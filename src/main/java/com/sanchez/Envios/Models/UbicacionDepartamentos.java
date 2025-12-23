package com.sanchez.Envios.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_ubicacion_departamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionDepartamentos {

    @Id
    @GeneratedValue
    @Column(name = "tb_ubicacion_departamentos_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_ubicacion_departamentos_nombre", nullable = false)
    private String nombre;

    @Column(name = "tb_ubicacion_departamentos_codigo")
    private String codigo;

    @Column(name = "tb_ubicacion_departamentos_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_ubicacion_departamentos_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_ubicacion_departamentos_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_ubicacion_departamentos_usuario_actualizacion")
    private UUID usuarioActualizacion;
}
