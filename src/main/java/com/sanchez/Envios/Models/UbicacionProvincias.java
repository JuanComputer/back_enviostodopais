package com.sanchez.Envios.Models;

import com.sanchez.Envios.Models.UbicacionDepartamentos;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_ubicacion_provincias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionProvincias {

    @Id
    @GeneratedValue
    @Column(name = "tb_ubicacion_provincias_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_ubicacion_provincias_nombre", nullable = false)
    private String nombre;

    @Column(name = "tb_ubicacion_provincias_codigo")
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "tb_ubicacion_departamentos_id")
    private UbicacionDepartamentos departamento;

    @Column(name = "tb_ubicacion_provincias_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_ubicacion_provincias_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_ubicacion_provincias_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_ubicacion_provincias_usuario_actualizacion")
    private UUID usuarioActualizacion;
}
