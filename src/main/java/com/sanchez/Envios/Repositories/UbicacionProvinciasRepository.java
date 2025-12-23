package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.UbicacionProvincias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UbicacionProvinciasRepository extends JpaRepository<UbicacionProvincias, UUID> {
    List<UbicacionProvincias> findByDepartamento_CodigoOrderByNombreAsc(String departamentoCodigo);

    UbicacionProvincias findByCodigo(String codigo);
}
