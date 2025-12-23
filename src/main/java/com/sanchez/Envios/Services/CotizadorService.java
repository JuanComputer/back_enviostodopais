package com.sanchez.Envios.Services;


import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.CotizadorTarifa;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Repositories.CotizadorTarifaRepository;
import com.sanchez.Envios.Repositories.TiendasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CotizadorService {

    @Autowired
    private CotizadorTarifaRepository tarifaRepository;

    @Autowired
    private TiendasRepository tiendasRepository;

    public ResponseDto<Map<String, Object>> calcularCotizacion(
            UUID origenId,
            UUID destinoId,
            BigDecimal peso,
            String tipoServicio,
            BigDecimal valorDeclarado
    ) {
        try {
            Optional<CotizadorTarifa> tarifaOpt =
                    tarifaRepository.findFirstByTipoServicio(tipoServicio);

            if (tarifaOpt.isEmpty()) {
                return new ResponseDto<>(404, "No se encontró tarifa para el tipo de servicio", null);
            }

            CotizadorTarifa tarifa = tarifaOpt.get();

            // Parámetros del modelo de costo
            BigDecimal tarifaBase = tarifa.getTarifaBase();          // p.ej. 10.00
            BigDecimal coefPeso = tarifa.getCostoPorKm();            // p.ej. 0.8
            BigDecimal recargoExpress = tipoServicio.equalsIgnoreCase("Express")
                    ? new BigDecimal("8.00")
                    : BigDecimal.ZERO;
            BigDecimal porcentajeSeguro = new BigDecimal("0.015");   // 1.5% del valor declarado

            // Cálculo del costo
            BigDecimal costo = tarifaBase
                    .add(coefPeso.multiply(peso))
                    .add(valorDeclarado.multiply(porcentajeSeguro))
                    .add(recargoExpress)
                    .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> data = new HashMap<>();
            data.put("precio", costo);
            data.put("diasEstimados", tarifa.getDiasEstimados());
            data.put("valorDeclarado", valorDeclarado);

            return new ResponseDto<>(200, "Cotización generada correctamente", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDto<>(500, "Error al calcular cotización: " + e.getMessage(), null);
        }
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}

