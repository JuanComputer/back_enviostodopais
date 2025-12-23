package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.Tiendas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TiendasRepository extends JpaRepository<Tiendas, UUID> {

    List<Tiendas> findByNombreContainingIgnoreCase(String nombre);
}