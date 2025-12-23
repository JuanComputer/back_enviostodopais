package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.EnvioRequestDto;
import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Envios;
import com.sanchez.Envios.Services.EnviosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin("*")
public class EnviosController {

    private EnviosService enviosService;

    @Autowired
    public EnviosController(EnviosService enviosService){
        this.enviosService=enviosService;
    }

    // ✅ Crear un envío (puede ser con o sin emisor)
    @PostMapping("/crear")
    @PreAuthorize("hasRole('Operador')")
    public ResponseDto<Envios> crearEnvio(@RequestBody EnvioRequestDto request) {
        return enviosService.crearEnvio(request);
    }

    // ✅ Listar envíos con filtros opcionales
    @GetMapping("/listar")
    public ResponseDto<List<Envios>> listarEnvios(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String dniReceptor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return enviosService.listarEnvios(estado, dniReceptor, fechaInicio, fechaFin);
    }

    // ✅ Buscar por código tracking
    @GetMapping("/tracking/{codigo}")
    @PreAuthorize("hasRole('Operador')")
    public ResponseDto<Envios> obtenerPorTracking(@PathVariable String codigo) {
        return enviosService.buscarPorTracking(codigo);
    }

    // ✅ Actualizar estado
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('Operador')")
    public ResponseDto<Envios> actualizarEstado(
            @PathVariable UUID id,
            @RequestParam String nuevoEstado
    ) {
        return enviosService.cambiarEstado(id, nuevoEstado);
    }

    // ✅ Editar datos de envío (receptor o destino)
    @PutMapping("/{id}/editar")
    public ResponseDto<Envios> editarEnvio(
            @PathVariable UUID id,
            @RequestParam(required = false) String receptorNombre,
            @RequestParam(required = false) String receptorDni,
            @RequestParam(required = false) UUID destinoId
    ) {
        return enviosService.editarEnvio(id, receptorNombre, receptorDni, destinoId);
    }

    // ✅ Eliminar envío
    @DeleteMapping("/{id}")
    public ResponseDto<String> eliminarEnvio(@PathVariable UUID id) {
        return enviosService.eliminarEnvio(id);
    }

}
