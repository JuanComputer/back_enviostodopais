package com.sanchez.Envios.Services;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Dto.TiendaRequestDto;
import com.sanchez.Envios.Models.Tiendas;

import com.sanchez.Envios.Repositories.TiendasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TiendasService {

    @Autowired
    private TiendasRepository tiendasRepository;

    // ✅ Crear tienda
    public ResponseDto<Tiendas> crearTienda(String nombre, String direccion, Double latitud, Double longitud) {
        if (nombre == null || direccion == null)
            return new ResponseDto<>(400, "Nombre y dirección son obligatorios", null);

        Tiendas tienda = Tiendas.builder()
                .nombre(nombre)
                .direccion(direccion)
                .latitud(BigDecimal.valueOf(latitud))
                .longitud(BigDecimal.valueOf(longitud))
                .fechaCreacion(LocalDateTime.now())
                .build();

        Tiendas guardada = tiendasRepository.save(tienda);
        return new ResponseDto<>(201, "Tienda registrada correctamente", guardada);
    }

    // ✅ Listar tiendas (con filtro opcional)
    public ResponseDto<List<Tiendas>> listarTiendas(String nombre) {
        List<Tiendas> tiendas = (nombre != null && !nombre.isEmpty())
                ? tiendasRepository.findByNombreContainingIgnoreCase(nombre)
                : tiendasRepository.findAll();

        return new ResponseDto<>(200, "Listado de tiendas", tiendas);
    }

    // ✅ Obtener tienda por ID
    public ResponseDto<Tiendas> obtenerPorId(UUID id) {
        Optional<Tiendas> tienda = tiendasRepository.findById(id);
        return tienda.map(value -> new ResponseDto<>(200, "Tienda encontrada", value))
                .orElseGet(() -> new ResponseDto<>(404, "Tienda no encontrada", null));
    }

    // ✅ Editar tienda
    public ResponseDto<Tiendas> editarTienda(UUID id, TiendaRequestDto tiendaRequestDto) {
        Optional<Tiendas> optional = tiendasRepository.findById(id);
        if (optional.isEmpty())
            return new ResponseDto<>(404, "Tienda no encontrada", null);

        Tiendas tienda = optional.get();

        if (tiendaRequestDto.getNombre() != null) tienda.setNombre(tiendaRequestDto.getNombre());
        if (tiendaRequestDto.getDireccion() != null) tienda.setDireccion(tiendaRequestDto.getDireccion());
        if (tiendaRequestDto.getLatitud() != null) tienda.setLatitud(BigDecimal.valueOf(tiendaRequestDto.getLatitud()));
        if (tiendaRequestDto.getLongitud() != null) tienda.setLongitud(BigDecimal.valueOf(tiendaRequestDto.getLongitud()));

        tienda.setFechaActualizacion(LocalDateTime.now());
        Tiendas actualizada = tiendasRepository.save(tienda);

        return new ResponseDto<>(200, "Tienda actualizada correctamente", actualizada);
    }

    // ✅ Eliminar tienda
    public ResponseDto<String> eliminarTienda(UUID id) {
        if (!tiendasRepository.existsById(id))
            return new ResponseDto<>(404, "Tienda no encontrada", null);

        tiendasRepository.deleteById(id);
        return new ResponseDto<>(200, "Tienda eliminada correctamente", "OK");
    }
}
