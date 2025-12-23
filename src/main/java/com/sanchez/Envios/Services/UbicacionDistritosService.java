package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.UbicacionDistritos;
import com.sanchez.Envios.Repositories.UbicacionDistritosRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UbicacionDistritosService {

    private final UbicacionDistritosRepository repository;

    public UbicacionDistritosService(UbicacionDistritosRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<UbicacionDistritos>> listar() {
        try {
            return new ResponseDto<>(200, "Distritos obtenidos correctamente", repository.findAll());
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al listar distritos: " + e.getMessage(), Collections.emptyList());
        }
    }

    public ResponseDto<UbicacionDistritos> crear(UbicacionDistritos distrito) {
        try {
            return new ResponseDto<>(201, "Distrito creado correctamente", repository.save(distrito));
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al crear distrito: " + e.getMessage(), null);
        }
    }
}
