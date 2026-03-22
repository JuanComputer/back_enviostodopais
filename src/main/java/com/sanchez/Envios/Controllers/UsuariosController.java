package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Dto.UsuarioRequestDto;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Services.UsuariosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuariosController {

    @Autowired private UsuariosService usuariosService;

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    private String correo() { return auth().getName(); }
    private String rol()    {
        return auth().getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
    }

    @GetMapping("/listar")
    public ResponseDto<List<Usuarios>> listar(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo) {
        return usuariosService.listar(correo(), rol(), rol, activo);
    }

    @PostMapping("/crear")
    public ResponseDto<Usuarios> crear(@RequestBody UsuarioRequestDto dto) {
        return usuariosService.crear(dto, rol());
    }

    @PutMapping("/{id}/editar")
    public ResponseDto<Usuarios> editar(@PathVariable UUID id,
                                         @RequestBody UsuarioRequestDto dto) {
        return usuariosService.editar(id, dto, rol());
    }

    @PutMapping("/{id}/toggle-activo")
    public ResponseDto<Usuarios> toggleActivo(@PathVariable UUID id) {
        return usuariosService.toggleActivo(id, rol());
    }

    @GetMapping("/estadisticas")
    public ResponseDto<Map<String, Object>> estadisticas() {
        return usuariosService.estadisticas(correo(), rol());
    }
}
