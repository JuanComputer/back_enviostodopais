package com.sanchez.Envios.Repositories;


import com.sanchez.Envios.Models.CotizadorTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CotizadorTarifaRepository extends JpaRepository<CotizadorTarifa, UUID> {

    Optional<CotizadorTarifa> findFirstByTipoServicioAndPesoMinLessThanEqualAndPesoMaxGreaterThanEqual(
            String tipoServicio, BigDecimal pesoMin, BigDecimal pesoMax);

    Optional<CotizadorTarifa> findFirstByTipoServicio(String tipoServicio);
}

