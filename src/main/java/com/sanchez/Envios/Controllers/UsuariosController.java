package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Services.UsuariosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuariosController {

    private final UsuariosService usuariosService;

    public UsuariosController(UsuariosService usuariosService) {
        this.usuariosService = usuariosService;
    }

    @GetMapping("/listar")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ResponseDto<List<Usuarios>>> listarUsuarios(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo) {

        ResponseDto<List<Usuarios>> response = usuariosService.listarUsuarios(rol, activo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ResponseDto<Map<String, Long>>> obtenerEstadisticas() {
        try {
            ResponseDto<Map<String,Long>> resp = usuariosService.obtenerEstadisticas();
            return ResponseEntity.status(resp.getStatusCode()).body(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ResponseDto<>(500, "Error al obtener estadísticas", null));
        }
    }
}
