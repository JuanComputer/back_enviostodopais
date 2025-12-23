package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.UbicacionDepartamentos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UbicacionDepartamentosRepository extends JpaRepository<UbicacionDepartamentos, UUID> {

    List<UbicacionDepartamentos> findAllByOrderByNombreAsc();

    UbicacionDepartamentos findByCodigo(String codigo);
}