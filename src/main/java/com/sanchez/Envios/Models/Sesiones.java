package com.sanchez.Envios.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "tb_sesiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesiones {

    @Id
    @GeneratedValue
    @Column(name = "tb_sesiones_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tb_usuarios_id", nullable = false)
    private Usuarios usuario;

    @Column(name = "tb_sesiones_token")
    private String token;

    @Column(name = "tb_sesiones_fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "tb_sesiones_activo")
    private Boolean activo;

    @Column(name = "tb_sesiones_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_sesiones_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_sesiones_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_sesiones_usuario_actualizacion")
    private UUID usuarioActualizacion;
}