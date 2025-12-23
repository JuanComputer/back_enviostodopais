package com.sanchez.Envios.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "tb_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roles {

    @Id
    @GeneratedValue
    @Column(name = "tb_roles_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_roles_nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "tb_roles_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_roles_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_roles_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_roles_usuario_actualizacion")
    private UUID usuarioActualizacion;
}