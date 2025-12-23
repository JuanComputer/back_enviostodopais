package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Informes;
import com.sanchez.Envios.Repositories.InformesRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InformesService {

    private final InformesRepository repository;

    public InformesService(InformesRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<Informes>> listar() {
        try {
            return new ResponseDto<>(200, "Informes obtenidos correctamente", repository.findAll());
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al listar informes: " + e.getMessage(), Collections.emptyList());
        }
    }

    public ResponseDto<Informes> crear(Informes informe) {
        try {
            return new ResponseDto<>(201, "Informe creado correctamente", repository.save(informe));
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al crear informe: " + e.getMessage(), null);
        }
    }
}

