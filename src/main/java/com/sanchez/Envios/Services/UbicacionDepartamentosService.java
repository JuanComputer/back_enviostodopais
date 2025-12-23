package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.UbicacionDepartamentos;
import com.sanchez.Envios.Repositories.UbicacionDepartamentosRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UbicacionDepartamentosService {

    private final UbicacionDepartamentosRepository repository;

    public UbicacionDepartamentosService(UbicacionDepartamentosRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<UbicacionDepartamentos>> listarDepartamentos(String filtroNombre) {
        try {
            List<UbicacionDepartamentos> lista = repository.findAll();

            if (filtroNombre != null && !filtroNombre.isEmpty()) {
                lista = lista.stream()
                        .filter(d -> d.getNombre().toLowerCase().contains(filtroNombre.toLowerCase()))
                        .collect(Collectors.toList());
            }

            return ResponseDto.<List<UbicacionDepartamentos>>builder()
                    .statusCode(200)
                    .message("Departamentos obtenidos correctamente")
                    .data(lista)
                    .build();

        } catch (Exception e) {
            return ResponseDto.<List<UbicacionDepartamentos>>builder()
                    .statusCode(500)
                    .message("Error al listar departamentos: " + e.getMessage())
                    .data(Collections.emptyList())
                    .build();
        }
    }

    public ResponseDto<UbicacionDepartamentos> obtenerPorId(UUID id) {
        try {
            Optional<UbicacionDepartamentos> dep = repository.findById(id);
            if (dep.isPresent()) {
                return ResponseDto.<UbicacionDepartamentos>builder()
                        .statusCode(200)
                        .message("Departamento encontrado")
                        .data(dep.get())
                        .build();
            } else {
                return ResponseDto.<UbicacionDepartamentos>builder()
                        .statusCode(404)
                        .message("Departamento no encontrado")
                        .data(null)
                        .build();
            }
        } catch (Exception e) {
            return ResponseDto.<UbicacionDepartamentos>builder()
                    .statusCode(500)
                    .message("Error al buscar departamento: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    public ResponseDto<UbicacionDepartamentos> crear(UbicacionDepartamentos dep) {
        try {
            UbicacionDepartamentos nuevo = repository.save(dep);
            return ResponseDto.<UbicacionDepartamentos>builder()
                    .statusCode(201)
                    .message("Departamento creado correctamente")
                    .data(nuevo)
                    .build();
        } catch (Exception e) {
            return ResponseDto.<UbicacionDepartamentos>builder()
                    .statusCode(500)
                    .message("Error al crear departamento: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    public ResponseDto<Void> eliminar(UUID id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                return ResponseDto.<Void>builder()
                        .statusCode(200)
                        .message("Departamento eliminado correctamente")
                        .data(null)
                        .build();
            } else {
                return ResponseDto.<Void>builder()
                        .statusCode(404)
                        .message("Departamento no encontrado")
                        .data(null)
                        .build();
            }
        } catch (Exception e) {
            return ResponseDto.<Void>builder()
                    .statusCode(500)
                    .message("Error al eliminar departamento: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
}
