package com.sanchez.Envios.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "tb_informes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Informes {

    @Id
    @GeneratedValue
    @Column(name = "tb_informes_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tb_envios_id")
    private Envios envio;

    @ManyToOne
    @JoinColumn(name = "tb_usuarios_id")
    private Usuarios usuario;

    @Column(name = "tb_informes_problema")
    private String problema;

    @Column(name = "tb_informes_acciones")
    private String acciones;

    @Column(name = "tb_informes_observaciones")
    private String observaciones;

    @Column(name = "tb_informes_estado")
    private String estado;

    @Column(name = "tb_informes_fecha")
    private LocalDateTime fecha;

    @Column(name = "tb_informes_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_informes_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_informes_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_informes_usuario_actualizacion")
    private UUID usuarioActualizacion;
}