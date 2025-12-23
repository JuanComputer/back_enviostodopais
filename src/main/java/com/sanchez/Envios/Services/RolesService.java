package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Roles;
import com.sanchez.Envios.Repositories.RolesRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RolesService {

    private final RolesRepository repository;

    public RolesService(RolesRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<Roles>> listar() {
        try {
            return ResponseDto.<List<Roles>>builder()
                    .statusCode(200)
                    .message("Roles obtenidos correctamente")
                    .data(repository.findAll())
                    .build();
        } catch (Exception e) {
            return ResponseDto.<List<Roles>>builder()
                    .statusCode(500)
                    .message("Error al listar roles: " + e.getMessage())
                    .data(Collections.emptyList())
                    .build();
        }
    }

    public ResponseDto<Roles> crear(Roles rol) {
        try {
            return ResponseDto.<Roles>builder()
                    .statusCode(201)
                    .message("Rol creado correctamente")
                    .data(repository.save(rol))
                    .build();
        } catch (Exception e) {
            return ResponseDto.<Roles>builder()
                    .statusCode(500)
                    .message("Error al crear rol: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    public ResponseDto<Roles> obtenerPorId(UUID id) {
        try {
            return repository.findById(id)
                    .map(r -> ResponseDto.<Roles>builder()
                            .statusCode(200)
                            .message("Rol encontrado")
                            .data(r)
                            .build())
                    .orElse(ResponseDto.<Roles>builder()
                            .statusCode(404)
                            .message("Rol no encontrado")
                            .data(null)
                            .build());
        } catch (Exception e) {
            return ResponseDto.<Roles>builder()
                    .statusCode(500)
                    .message("Error al obtener rol: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    public ResponseDto<Void> eliminar(UUID id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                return new ResponseDto<>(200, "Rol eliminado correctamente", null);
            }
            return new ResponseDto<>(404, "Rol no encontrado", null);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al eliminar rol: " + e.getMessage(), null);
        }
    }
}
