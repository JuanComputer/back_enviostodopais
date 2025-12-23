package com.sanchez.Envios.Dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiendaRequestDto {


    String nombre;
    String direccion;
    Double latitud;
    Double longitud;
}
