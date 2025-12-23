package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Dto.TiendaRequestDto;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Services.TiendasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tiendas")
@CrossOrigin("*")
public class TiendasController {

    private TiendasService tiendasService;

    @Autowired
    public TiendasController(TiendasService tiendasService){
        this.tiendasService=tiendasService;
    }

    // ✅ Crear
    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('Operador')")
    public ResponseDto<Tiendas> crearTienda(@RequestBody TiendaRequestDto tiendaRequestDto) {
        return tiendasService.crearTienda(tiendaRequestDto.getNombre(), tiendaRequestDto.getDireccion(), tiendaRequestDto.getLatitud(), tiendaRequestDto.getLongitud());
    }

    // ✅ Listar
    @GetMapping("/listar")
    public ResponseDto<List<Tiendas>> listarTiendas(@RequestParam(required = false) String nombre) {
        return tiendasService.listarTiendas(nombre);
    }

    // ✅ Obtener por ID
    @GetMapping("/{id}")
    public ResponseDto<Tiendas> obtenerPorId(@PathVariable UUID id) {
        return tiendasService.obtenerPorId(id);
    }

    // ✅ Editar
    @PutMapping("/{id}/editar")
    public ResponseDto<Tiendas> editarTienda(
            @PathVariable UUID id,
            @RequestBody TiendaRequestDto tiendaRequestDto
    ) {
        return tiendasService.editarTienda(id,tiendaRequestDto);
    }

    // ✅ Eliminar
    @DeleteMapping("/{id}")
    public ResponseDto<String> eliminarTienda(@PathVariable UUID id) {
        return tiendasService.eliminarTienda(id);
    }
}
