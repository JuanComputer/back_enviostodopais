package com.sanchez.Envios.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "tb_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuarios {

    @Id
    @GeneratedValue
    @Column(name = "tb_usuarios_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;



    @Column(name = "tb_usuarios_name", nullable = false, unique = true)
    private String nombre;

    @Column(name = "tb_usuarios_apellido_paterno", nullable = false, unique = true)
    private String apellidoP;

    @Column(name = "tb_usuarios_apellido_materno", nullable = false, unique = true)
    private String apellidoM;

    @Column(name = "tb_usuarios_correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "tb_usuarios_password", nullable = false)
    private String password;

    @Column(name = "tb_usuarios_dni", length = 8, nullable = false, unique = true)
    private String dni;

    @ManyToOne
    @JoinColumn(name = "tb_roles_id", nullable = false)
    private Roles rol;

    @Column(name = "tb_usuarios_activo")
    private Boolean activo;

    @Column(name = "tb_usuarios_fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "tb_usuarios_fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "tb_usuarios_usuario_creacion")
    private UUID usuarioCreacion;

    @Column(name = "tb_usuarios_usuario_actualizacion")
    private UUID usuarioActualizacion;

    // Método para obtener el nombre completo
    public String getNombreCompleto() {
        return String.format("%s %s %s", nombre, apellidoP, apellidoM).trim();
    }
}