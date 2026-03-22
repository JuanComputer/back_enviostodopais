package com.sanchez.Envios.Dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UsuarioRequestDto {
    private String nombre;
    private String apellidoP;
    private String apellidoM;
    private String correo;
    private String password;
    private String dni;
    private String rolNombre;   // nombre del rol
    private UUID   sedeId;      // sede asignada (para Operadores y Admin Sede)
    private Boolean activo;
}
