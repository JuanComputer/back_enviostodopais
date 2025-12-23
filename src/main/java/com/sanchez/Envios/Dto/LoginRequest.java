package com.sanchez.Envios.Dto;

import lombok.*;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    private String correo;
    private String password;
}