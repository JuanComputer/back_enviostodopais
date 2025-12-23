package com.sanchez.Envios.Services;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Repositories.UsuariosRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsuariosService {

    private final UsuariosRepository repository;

    public UsuariosService(UsuariosRepository repository) {
        this.repository = repository;
    }

    public ResponseDto<List<Usuarios>> listarUsuarios(String rol, Boolean activo) {
        try {
            List<Usuarios> usuarios;

            if (rol != null && activo != null) {
                usuarios = repository.findByRolNombreAndActivo(rol, activo);
            } else if (rol != null) {
                usuarios = repository.findByRolNombre(rol);
            } else if (activo != null) {
                usuarios = repository.findByActivo(activo);
            } else {
                usuarios = repository.findAll();
            }

            return new ResponseDto<>(200, "Listado de usuarios obtenido correctamente", usuarios);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al listar usuarios: " + e.getMessage(), null);
        }
    }

    public ResponseDto<Usuarios> obtenerPorId(UUID id) {
        try {
            return repository.findById(id)
                    .map(user -> ResponseDto.<Usuarios>builder()
                            .statusCode(200)
                            .message("Usuario encontrado")
                            .data(user)
                            .build())
                    .orElse(ResponseDto.<Usuarios>builder()
                            .statusCode(404)
                            .message("Usuario no encontrado")
                            .data(null)
                            .build());
        } catch (Exception e) {
            return ResponseDto.<Usuarios>builder()
                    .statusCode(500)
                    .message("Error al obtener usuario: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    public ResponseDto<Usuarios> actualizarUsuario(UUID id, Usuarios datos) {
        try {
            Optional<Usuarios> optional = repository.findById(id);
            if (optional.isEmpty()) {
                return new ResponseDto<>(404, "Usuario no encontrado", null);
            }

            Usuarios usuario = optional.get();
            usuario.setCorreo(datos.getCorreo());
            usuario.setActivo(datos.getActivo());
            usuario.setRol(datos.getRol()); // o buscar rol en BD

            repository.save(usuario);
            return new ResponseDto<>(200, "Usuario actualizado correctamente", usuario);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al actualizar: " + e.getMessage(), null);
        }
    }

    public ResponseDto<Void> eliminarUsuario(UUID id) {
        try {
            if (!repository.existsById(id)) {
                return new ResponseDto<>(404, "Usuario no encontrado", null);
            }
            repository.deleteById(id);
            return new ResponseDto<>(200, "Usuario eliminado correctamente", null);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al eliminar: " + e.getMessage(), null);
        }
    }


    public ResponseDto<Map<String,Long>> obtenerEstadisticas(){
        try {

            Map<String, Long> stats = new HashMap<>();
            stats.put("total", repository.count());
            stats.put("activos", repository.countByActivo(true));
            stats.put("inactivos", repository.countByActivo(false));
            return new ResponseDto<>(200, "Estadísticas obtenidas", stats);

        }catch (Exception e){
            return new ResponseDto<>(500,"Error al traer estadisticas",null);
        }
    }

}
