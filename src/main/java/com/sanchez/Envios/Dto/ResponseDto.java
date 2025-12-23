package com.sanchez.Envios.Dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto<T> {
    private Integer statusCode;
    private String message;
    private T data;
}
