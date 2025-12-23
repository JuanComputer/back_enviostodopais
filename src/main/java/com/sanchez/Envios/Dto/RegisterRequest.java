package com.sanchez.Envios.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String name;
    private String apellidoP;
    private String apellidoM;
    private String correo;
    private String password;
    private String dni;
    private UUID rolId; // opcional, por defecto cliente si null
}