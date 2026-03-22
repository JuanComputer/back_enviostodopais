package com.sanchez.Envios.Repositories;

import com.sanchez.Envios.Models.Envios;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Models.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnviosRepository extends JpaRepository<Envios, UUID> {

    Optional<Envios> findByCodigoTracking(String codigoTracking);

    // Envíos por sede de origen o destino (para Admins de Sede y Operadores)
    List<Envios> findByOrigenOrDestino(Tiendas origen, Tiendas destino);

    // Envíos donde el emisor es un usuario registrado
    List<Envios> findByEmisor(Usuarios emisor);

    // Contar envíos del día para numeración correlativa
    @Query("SELECT COUNT(e) FROM Envios e WHERE e.tipoDocumento = :tipo " +
           "AND CAST(e.fechaCreacion AS date) = :fecha")
    long countByTipoDocumentoAndFecha(@Param("tipo") String tipo,
                                      @Param("fecha") LocalDate fecha);
}
