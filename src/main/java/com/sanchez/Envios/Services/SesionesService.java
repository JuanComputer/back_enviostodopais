package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Sesiones;
import com.sanchez.Envios.Repositories.SesionesRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SesionesService {

    private final SesionesRepository repository;

    public SesionesService(SesionesRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<Sesiones>> listar() {
        try {
            return new ResponseDto<>(200, "Sesiones obtenidas correctamente", repository.findAll());
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al listar sesiones: " + e.getMessage(), Collections.emptyList());
        }
    }

    public ResponseDto<Sesiones> crear(Sesiones sesion) {
        try {
            return new ResponseDto<>(201, "Sesión creada correctamente", repository.save(sesion));
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al crear sesión: " + e.getMessage(), null);
        }
    }
}

