package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Dto.TiendaRequestDto;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Services.TiendasService;
import com.sanchez.Envios.Services.UsuariosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tiendas")
@CrossOrigin("*")
public class TiendasController {

    @Autowired private TiendasService tiendasService;

    private String rol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "";
        return auth.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
    }

    // Público — listar y obtener por ID
    @GetMapping("/listar")
    public ResponseDto<List<Tiendas>> listar(@RequestParam(required = false) String nombre) {
        return tiendasService.listarTiendas(nombre);
    }

    @GetMapping("/{id}")
    public ResponseDto<Tiendas> obtener(@PathVariable UUID id) {
        return tiendasService.obtenerPorId(id);
    }

    // Solo Admin General puede crear sedes
    @PostMapping("/crear")
    public ResponseDto<Tiendas> crear(@RequestBody TiendaRequestDto dto) {
        String r = rol();
        if (!r.contains("Administrador General"))
            return new ResponseDto<>(403, "Solo el Administrador General puede crear sedes", null);
        return tiendasService.crearTienda(dto.getNombre(), dto.getDireccion(),
                dto.getLatitud(), dto.getLongitud());
    }

    // Admin General edita cualquier sede, Admin Sede solo la suya
    @PutMapping("/{id}/editar")
    public ResponseDto<Tiendas> editar(@PathVariable UUID id,
                                        @RequestBody TiendaRequestDto dto) {
        String r = rol();
        if (!r.contains("Administrador General") && !r.contains("Administrador de Sede"))
            return new ResponseDto<>(403, "No tienes permiso para editar sedes", null);
        return tiendasService.editarTienda(id, dto);
    }

    // Solo Admin General puede eliminar
    @DeleteMapping("/{id}")
    public ResponseDto<String> eliminar(@PathVariable UUID id) {
        if (!rol().contains("Administrador General"))
            return new ResponseDto<>(403, "Solo el Administrador General puede eliminar sedes", null);
        return tiendasService.eliminarTienda(id);
    }
}
