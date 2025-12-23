    package com.sanchez.Envios.Controllers;


    import com.sanchez.Envios.Dto.ResponseDto;
    import com.sanchez.Envios.Services.CotizadorService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;

    import java.math.BigDecimal;
    import java.util.Map;
    import java.util.UUID;

    @RestController
    @RequestMapping("/api/cotizador")
    @CrossOrigin("*")
    public class CotizadorController {

        @Autowired
        private CotizadorService cotizadorService;

        @PostMapping("/calcular")
        public ResponseDto<Map<String, Object>> calcularCotizacion(@RequestBody Map<String, Object> payload) {
            UUID origenId = UUID.fromString(payload.get("origenId").toString());
            UUID destinoId = UUID.fromString(payload.get("destinoId").toString());
            BigDecimal peso = new BigDecimal(payload.get("peso").toString());
            String tipoServicio = payload.get("tipoServicio").toString();
            BigDecimal valorDeclarado = new BigDecimal(payload.get("valorDeclarado").toString());

            return cotizadorService.calcularCotizacion(origenId, destinoId, peso, tipoServicio,valorDeclarado);
        }

    }
