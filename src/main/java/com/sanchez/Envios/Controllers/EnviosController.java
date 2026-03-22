package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.CambioEstadoDto;
import com.sanchez.Envios.Dto.EnvioRequestDto;
import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Envios;
import com.sanchez.Envios.Services.BoletaService;
import com.sanchez.Envios.Services.EnviosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin("*")
public class EnviosController {

    @Autowired private EnviosService enviosService;
    @Autowired private BoletaService boletaService;

    /** Registrar envío (Operador / Admin de Sede / Admin General) */
    @PostMapping("/crear")
    public ResponseDto<Envios> crear(@RequestBody EnvioRequestDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        String rol    = auth.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
        return enviosService.crearEnvio(dto, correo);
    }

    /** Listar envíos (filtrado automático por rol y sede) */
    @GetMapping("/listar")
    public ResponseDto<List<Envios>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String dniReceptor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        String rol    = auth.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
        return enviosService.listarEnvios(correo, rol, estado, dniReceptor);
    }

    /** Mis envíos (para clientes logueados) */
    @GetMapping("/mis-envios")
    public ResponseDto<List<Envios>> misEnvios() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return enviosService.misEnvios(auth.getName());
    }

    /** Tracking público (sin auth) */
    @GetMapping("/tracking/{codigo}")
    public ResponseDto<Envios> tracking(@PathVariable String codigo) {
        return enviosService.buscarPorTracking(codigo);
    }

    /** Estados válidos para un envío específico */
    @GetMapping("/{id}/estados-validos")
    public ResponseDto<List<String>> estadosValidos(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rol = auth.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
        return enviosService.estadosPermitidos(id, rol);
    }

    /** Cambiar estado con nota opcional */
    @PutMapping("/{id}/estado")
    public ResponseDto<Envios> cambiarEstado(@PathVariable UUID id,
                                              @RequestBody CambioEstadoDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        String rol    = auth.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
        return enviosService.cambiarEstado(id, dto, correo, rol);
    }

    /** Generar boleta/factura en base64 */
    @GetMapping("/{id}/boleta")
    public ResponseDto<Map<String, String>> boleta(@PathVariable UUID id) {
        return boletaService.generarBoletaPdf(id);
    }
}
