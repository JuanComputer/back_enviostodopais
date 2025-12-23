package com.sanchez.Envios.Models;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_cotizador_tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizadorTarifa {

    @Id
    @GeneratedValue
    @Column(name = "tb_cotizador_tarifa_id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tb_cotizador_tipo_servicio", nullable = false)
    private String tipoServicio; // Ej: Normal, Express

    @Column(name = "tb_cotizador_peso_min", nullable = false)
    private BigDecimal pesoMin;

    @Column(name = "tb_cotizador_peso_max", nullable = false)
    private BigDecimal pesoMax;

    @Column(name = "tb_cotizador_tarifa_base", nullable = false)
    private BigDecimal tarifaBase;

    @Column(name = "tb_cotizador_costo_km", nullable = false)
    private BigDecimal costoPorKm;

    @Column(name = "tb_cotizador_dias_estimados", nullable = false)
    private Integer diasEstimados;
}
