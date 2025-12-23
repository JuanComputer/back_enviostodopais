package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.UbicacionDistritos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UbicacionDistritosRepository extends JpaRepository<UbicacionDistritos, UUID> {
    List<UbicacionDistritos> findByProvincia_CodigoOrderByNombreAsc(String provinciaCodigo);
}