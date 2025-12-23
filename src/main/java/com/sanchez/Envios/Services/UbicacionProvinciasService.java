package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.UbicacionProvincias;
import com.sanchez.Envios.Repositories.UbicacionProvinciasRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UbicacionProvinciasService {

    private final UbicacionProvinciasRepository repository;

    public UbicacionProvinciasService(UbicacionProvinciasRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<UbicacionProvincias>> listar() {
        try {
            return new ResponseDto<>(200, "Provincias obtenidas correctamente", repository.findAll());
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al listar provincias: " + e.getMessage(), Collections.emptyList());
        }
    }

    public ResponseDto<UbicacionProvincias> crear(UbicacionProvincias provincia) {
        try {
            return new ResponseDto<>(201, "Provincia creada correctamente", repository.save(provincia));
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al crear provincia: " + e.getMessage(), null);
        }
    }
}

