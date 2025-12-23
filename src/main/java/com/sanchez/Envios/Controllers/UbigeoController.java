package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Services.UbigeoService;

import com.sanchez.Envios.Models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubigeo")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UbigeoController {

    private final UbigeoService service;

    @GetMapping("/departamentos")
    public ResponseDto<List<UbicacionDepartamentos>> listarDepartamentos() {
        return service.listarDepartamentos();
    }

    @GetMapping("/provincias")
    public ResponseDto<List<UbicacionProvincias>> listarProvincias(
            @RequestParam String departamentoCodigo) {
        return service.listarProvincias(departamentoCodigo);
    }

    @GetMapping("/distritos")
    public ResponseDto<List<UbicacionDistritos>> listarDistritos(
            @RequestParam String provinciaCodigo) {
        return service.listarDistritos(provinciaCodigo);
    }
}
