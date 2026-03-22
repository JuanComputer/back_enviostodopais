package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Envios;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Repositories.EnviosRepository;
import com.sanchez.Envios.Repositories.TiendasRepository;
import com.sanchez.Envios.Repositories.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin("*")
public class ReportesController {

    @Autowired private EnviosRepository enviosRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private TiendasRepository tiendasRepository;

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    private String correo() { return auth().getName(); }
    private String rol()    {
        return auth().getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
    }

    @GetMapping("/resumen")
    public ResponseDto<Map<String, Object>> resumen() {
        try {
            boolean esAdminGeneral = rol().contains("Administrador General");
            Map<String, Object> data = new LinkedHashMap<>();

            List<Envios> envios;
            if (esAdminGeneral) {
                envios = enviosRepository.findAll();
            } else {
                Usuarios yo = usuariosRepository.findByCorreo(correo()).orElseThrow();
                Tiendas sede = yo.getSede();
                if (sede == null) return new ResponseDto<>(400, "Sin sede asignada", null);
                envios = enviosRepository.findByOrigenOrDestino(sede, sede);
                data.put("sede", sede.getNombre());
            }

            // Totales generales
            data.put("totalEnvios", envios.size());
            data.put("entregados",  envios.stream().filter(e -> "Entregado".equals(e.getEstado())).count());
            data.put("enTransito",  envios.stream().filter(e -> "En tránsito".equals(e.getEstado())).count());
            data.put("registrados", envios.stream().filter(e -> "Registrado".equals(e.getEstado())).count());
            data.put("cancelados",  envios.stream().filter(e -> "Cancelado".equals(e.getEstado())).count());
            data.put("noEntregados",envios.stream().filter(e -> "No entregado".equals(e.getEstado())).count());

            // Ingresos totales
            BigDecimal ingresos = envios.stream()
                    .filter(e -> e.getPrecioEnvio() != null)
                    .map(Envios::getPrecioEnvio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.put("ingresosTotal", ingresos);

            // Por tipo de servicio
            Map<String, Long> porServicio = envios.stream()
                    .filter(e -> e.getTipoServicio() != null)
                    .collect(Collectors.groupingBy(Envios::getTipoServicio, Collectors.counting()));
            data.put("porTipoServicio", porServicio);

            // Por tipo de entrega
            Map<String, Long> porEntrega = envios.stream()
                    .filter(e -> e.getTipoEntrega() != null)
                    .collect(Collectors.groupingBy(Envios::getTipoEntrega, Collectors.counting()));
            data.put("porTipoEntrega", porEntrega);

            // Por estado
            Map<String, Long> porEstado = envios.stream()
                    .collect(Collectors.groupingBy(Envios::getEstado, Collectors.counting()));
            data.put("porEstado", porEstado);

            // Solo Admin General: estadísticas globales de sedes y usuarios
            if (esAdminGeneral) {
                data.put("totalSedes",    tiendasRepository.count());
                data.put("totalUsuarios", usuariosRepository.count());
                data.put("usuariosActivos", usuariosRepository.countByActivo(true));
            }

            return new ResponseDto<>(200, "OK", data);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }
}
