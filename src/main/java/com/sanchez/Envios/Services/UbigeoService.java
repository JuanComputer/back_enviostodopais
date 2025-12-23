package com.sanchez.Envios.Services;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.UbicacionDepartamentos;
import com.sanchez.Envios.Models.UbicacionProvincias;
import com.sanchez.Envios.Models.UbicacionDistritos;
import com.sanchez.Envios.Repositories.UbicacionDepartamentosRepository;
import com.sanchez.Envios.Repositories.UbicacionProvinciasRepository;
import com.sanchez.Envios.Repositories.UbicacionDistritosRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbigeoService {

    private final UbicacionDepartamentosRepository departamentosRepo;
    private final UbicacionProvinciasRepository provinciasRepo;
    private final UbicacionDistritosRepository distritosRepo;

    public ResponseDto<List<UbicacionDepartamentos>> listarDepartamentos() {
        return new ResponseDto<>(200, "Departamentos cargados",
                departamentosRepo.findAllByOrderByNombreAsc());
    }

    public ResponseDto<List<UbicacionProvincias>> listarProvincias(String departamentoCodigo) {
        return new ResponseDto<>(200, "Provincias cargadas",
                provinciasRepo.findByDepartamento_CodigoOrderByNombreAsc(departamentoCodigo));
    }

    public ResponseDto<List<UbicacionDistritos>> listarDistritos(String provinciaCodigo) {
        return new ResponseDto<>(200, "Distritos cargados",
                distritosRepo.findByProvincia_CodigoOrderByNombreAsc(provinciaCodigo));
    }
}
