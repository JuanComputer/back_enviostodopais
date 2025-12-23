package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.Envios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnviosRepository extends JpaRepository<Envios, UUID> {



    @Query("SELECT t FROM Envios t where t.codigoTracking=:codigoTracking")
    Optional<Envios> findByCodigoTracking(String codigoTracking);

    List<Envios> findByEstadoIgnoreCase(String estado);

    List<Envios> findByReceptorDni(String receptorDni);
}