package com.sanchez.Envios.Services;

import com.sanchez.Envios.Dto.CambioEstadoDto;
import com.sanchez.Envios.Dto.EnvioRequestDto;
import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.*;
import com.sanchez.Envios.Repositories.*;
import com.sanchez.Envios.Util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EnviosService {

    // Estados válidos del sistema
    public static final String ST_REGISTRADO          = "Registrado";
    public static final String ST_RECIBIDO_SEDE       = "Recibido en sede";
    public static final String ST_EN_TRANSITO         = "En tránsito";
    public static final String ST_EN_SEDE_DESTINO     = "En sede de destino";
    public static final String ST_LISTO_RECOGER       = "Listo para recoger";
    public static final String ST_EN_REPARTO          = "En reparto";
    public static final String ST_ENTREGADO           = "Entregado";
    public static final String ST_NO_ENTREGADO        = "No entregado";
    public static final String ST_CANCELADO           = "Cancelado";

    // Transiciones válidas por tipo de entrega
    private static final Map<String, List<String>> TRANSICIONES_SEDE = new LinkedHashMap<>();
    private static final Map<String, List<String>> TRANSICIONES_DOMICILIO = new LinkedHashMap<>();

    static {
        TRANSICIONES_SEDE.put(ST_REGISTRADO,      List.of(ST_RECIBIDO_SEDE, ST_CANCELADO));
        TRANSICIONES_SEDE.put(ST_RECIBIDO_SEDE,   List.of(ST_EN_TRANSITO, ST_CANCELADO));
        TRANSICIONES_SEDE.put(ST_EN_TRANSITO,     List.of(ST_EN_SEDE_DESTINO));
        TRANSICIONES_SEDE.put(ST_EN_SEDE_DESTINO, List.of(ST_LISTO_RECOGER));
        TRANSICIONES_SEDE.put(ST_LISTO_RECOGER,   List.of(ST_ENTREGADO));
        TRANSICIONES_SEDE.put(ST_ENTREGADO,       Collections.emptyList());
        TRANSICIONES_SEDE.put(ST_CANCELADO,       Collections.emptyList());

        TRANSICIONES_DOMICILIO.put(ST_REGISTRADO,      List.of(ST_RECIBIDO_SEDE, ST_CANCELADO));
        TRANSICIONES_DOMICILIO.put(ST_RECIBIDO_SEDE,   List.of(ST_EN_TRANSITO, ST_CANCELADO));
        TRANSICIONES_DOMICILIO.put(ST_EN_TRANSITO,     List.of(ST_EN_SEDE_DESTINO));
        TRANSICIONES_DOMICILIO.put(ST_EN_SEDE_DESTINO, List.of(ST_EN_REPARTO));
        TRANSICIONES_DOMICILIO.put(ST_EN_REPARTO,      List.of(ST_ENTREGADO, ST_NO_ENTREGADO));
        TRANSICIONES_DOMICILIO.put(ST_NO_ENTREGADO,    List.of(ST_EN_REPARTO));
        TRANSICIONES_DOMICILIO.put(ST_ENTREGADO,       Collections.emptyList());
        TRANSICIONES_DOMICILIO.put(ST_CANCELADO,       Collections.emptyList());
    }

    @Autowired private EnviosRepository enviosRepository;
    @Autowired private TiendasRepository tiendasRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private EmailService emailService;
    @Autowired private CotizadorService cotizadorService;
    @Autowired private BoletaService boletaService;

    // ═══════════════════════════════════════════════
    // CREAR ENVÍO
    // ═══════════════════════════════════════════════
    public ResponseDto<Envios> crearEnvio(EnvioRequestDto dto, String correoOperador) {
        try {
            // 1. Obtener operador y su sede (origen automático)
            Usuarios operador = usuariosRepository.findByCorreo(correoOperador)
                    .orElseThrow(() -> new RuntimeException("Operador no encontrado"));
            Tiendas origen = operador.getSede();
            if (origen == null) {
                return new ResponseDto<>(400, "El operador no tiene sede asignada", null);
            }

            // 2. Validaciones básicas
            if (dto.getPeso() == null || dto.getPeso().compareTo(BigDecimal.ZERO) <= 0)
                return new ResponseDto<>(400, "El peso es obligatorio y debe ser mayor a 0", null);
            if (dto.getValorDeclarado() == null || dto.getValorDeclarado().compareTo(BigDecimal.ZERO) < 0)
                return new ResponseDto<>(400, "El valor declarado es obligatorio", null);
            if (dto.getReceptorNombre() == null || dto.getReceptorNombre().isBlank())
                return new ResponseDto<>(400, "El nombre del receptor es obligatorio", null);
            if (dto.getReceptorDni() == null || dto.getReceptorDni().isBlank())
                return new ResponseDto<>(400, "El DNI/RUC del receptor es obligatorio", null);
            if ("FACTURA".equalsIgnoreCase(dto.getTipoDocumento())) {
                String dniEmisor = dto.getEmisorDni();
                if (dniEmisor == null || dniEmisor.length() != 11)
                    return new ResponseDto<>(400, "La factura requiere RUC del emisor (11 dígitos)", null);
            }

            // 3. Sede destino
            Tiendas destino = null;
            if (dto.getDestinoId() != null)
                destino = tiendasRepository.findById(dto.getDestinoId()).orElse(null);
            if ("SEDE".equalsIgnoreCase(dto.getTipoEntrega()) && destino == null)
                return new ResponseDto<>(400, "Debe seleccionar una sede de destino", null);

            // 4. Calcular precio automáticamente
            String tipoServicio = dto.getTipoServicio() != null ? dto.getTipoServicio() : "Estandar";
            ResponseDto<Map<String, Object>> cotizacion = cotizadorService.calcularCotizacion(
                    origen.getId(), destino != null ? destino.getId() : origen.getId(),
                    dto.getPeso(), tipoServicio, dto.getValorDeclarado()
            );
            if (cotizacion.getStatusCode() != 200)
                return new ResponseDto<>(500, "Error al calcular el precio", null);
            BigDecimal precio = new BigDecimal(cotizacion.getData().get("precio").toString());
            int diasEstimados = (int) cotizacion.getData().get("diasEstimados");

            // 5. Emisor
            Usuarios emisorRegistrado = null;
            if (dto.getEmisorId() != null)
                emisorRegistrado = usuariosRepository.findById(dto.getEmisorId()).orElse(null);

            // 6. Número de documento con correlativo diario
            String tipoDoc = dto.getTipoDocumento() != null ? dto.getTipoDocumento().toUpperCase() : "BOLETA";
            String serie = "BOLETA".equals(tipoDoc) ? "B001" : "F001";
            LocalDate hoy = LocalDate.now();
            String fechaStr = hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long correlativo = enviosRepository.countByTipoDocumentoAndFecha(tipoDoc, hoy) + 1;
            String numeroDoc = String.format("%s-%s-%08d", serie, fechaStr, correlativo);

            // 7. Construir entidad
            Envios envio = new Envios();
            envio.setCodigoTracking("PKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            envio.setFechaCreacion(LocalDateTime.now());
            envio.setEstado(ST_REGISTRADO);
            envio.setOrigen(origen);
            envio.setDestino(destino);
            envio.setRegistradoPor(operador);
            envio.setTipoEntrega(dto.getTipoEntrega() != null ? dto.getTipoEntrega().toUpperCase() : "SEDE");
            envio.setDireccionEntrega(dto.getDireccionEntrega());
            envio.setReferenciaEntrega(dto.getReferenciaEntrega());
            envio.setPeso(dto.getPeso());
            envio.setValorDeclarado(dto.getValorDeclarado());
            envio.setDescripcionPaquete(dto.getDescripcionPaquete());
            envio.setTipoServicio(tipoServicio);
            envio.setTipoDocumento(tipoDoc);
            envio.setNumeroDocumento(numeroDoc);
            envio.setPrecioEnvio(precio);
            envio.setReceptorNombre(dto.getReceptorNombre());
            envio.setReceptorDni(dto.getReceptorDni());
            envio.setReceptorRazonSocial(dto.getReceptorRazonSocial());
            envio.setFechaEstimada(dto.getFechaEstimada() != null
                    ? dto.getFechaEstimada()
                    : hoy.plusDays(diasEstimados));

            if (emisorRegistrado != null) {
                envio.setEmisor(emisorRegistrado);
                envio.setEmisorNombre(emisorRegistrado.getNombreCompleto());
                envio.setEmisorDni(emisorRegistrado.getDni());
                envio.setEmisorCorreo(emisorRegistrado.getCorreo());
            } else {
                envio.setEmisorNombre(dto.getEmisorNombre());
                envio.setEmisorRazonSocial(dto.getEmisorRazonSocial());
                envio.setEmisorDni(dto.getEmisorDni());
                envio.setEmisorTelefono(dto.getEmisorTelefono());
                envio.setEmisorCorreo(dto.getEmisorCorreo());
            }

            Envios guardado = enviosRepository.save(envio);

            // 8. Generar PDF y enviar por correo
            if (guardado.getEmisorCorreo() != null && !guardado.getEmisorCorreo().isBlank()) {
                try {
                    byte[] pdfBytes = boletaService.generarPdfBytes(guardado);
                    String asunto = tipoDoc + " N° " + numeroDoc + " — Envios Todopais";
                    String cuerpo = buildCorreoConfirmacion(guardado);
                    emailService.enviarCorreoConPdf(
                            guardado.getEmisorCorreo(), asunto, cuerpo,
                            pdfBytes, numeroDoc + ".pdf");
                } catch (Exception e) {
                    System.err.println("Error al enviar correo con boleta: " + e.getMessage());
                }
            }

            return new ResponseDto<>(200, "Envío registrado correctamente", guardado);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDto<>(500, "Error al crear el envío: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════════════
    // CAMBIAR ESTADO (con validación de flujo)
    // ═══════════════════════════════════════════════
    public ResponseDto<Envios> cambiarEstado(UUID id, CambioEstadoDto dto,
                                              String correoUsuario, String rolUsuario) {
        try {
            Envios envio = enviosRepository.findById(id)
                    .orElse(null);
            if (envio == null)
                return new ResponseDto<>(404, "Envío no encontrado", null);

            String estadoActual = envio.getEstado();
            String nuevoEstado = dto.getNuevoEstado();

            // Validar transición según tipo de entrega
            Map<String, List<String>> transiciones = "DOMICILIO".equalsIgnoreCase(envio.getTipoEntrega())
                    ? TRANSICIONES_DOMICILIO : TRANSICIONES_SEDE;

            List<String> permitidos = transiciones.getOrDefault(estadoActual, Collections.emptyList());

            // Solo Admin General puede cancelar
            if (ST_CANCELADO.equals(nuevoEstado)) {
                boolean esAdminGeneral = rolUsuario != null &&
                        rolUsuario.contains("Administrador General");
                if (!esAdminGeneral)
                    return new ResponseDto<>(403, "Solo el Administrador General puede cancelar envíos", null);
                // Solo antes de En tránsito
                if (ST_EN_TRANSITO.equals(estadoActual) || ST_EN_SEDE_DESTINO.equals(estadoActual)
                        || ST_LISTO_RECOGER.equals(estadoActual) || ST_EN_REPARTO.equals(estadoActual)
                        || ST_ENTREGADO.equals(estadoActual))
                    return new ResponseDto<>(400, "No se puede cancelar un envío ya en tránsito o entregado", null);
            } else if (!permitidos.contains(nuevoEstado)) {
                return new ResponseDto<>(400,
                        "Transición no permitida: '" + estadoActual + "' → '" + nuevoEstado + "'. " +
                        "Estados válidos: " + permitidos, null);
            }

            // Nota obligatoria en "No entregado"
            if (ST_NO_ENTREGADO.equals(nuevoEstado) &&
                    (dto.getNota() == null || dto.getNota().isBlank()))
                return new ResponseDto<>(400, "Debe indicar el motivo de no entrega", null);

            envio.setEstado(nuevoEstado);
            envio.setNotaEstado(dto.getNota());
            envio.setFechaActualizacion(LocalDateTime.now());
            Envios actualizado = enviosRepository.save(envio);

            // Notificar al emisor por correo
            if (actualizado.getEmisorCorreo() != null && !actualizado.getEmisorCorreo().isBlank()) {
                String cuerpo = buildCorreoEstado(actualizado);
                emailService.enviarCorreo(actualizado.getEmisorCorreo(),
                        "Actualización de tu envío " + actualizado.getCodigoTracking(), cuerpo);
            }

            return new ResponseDto<>(200, "Estado actualizado correctamente", actualizado);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al cambiar estado: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════════════
    // LISTAR (filtrado por sede del usuario)
    // ═══════════════════════════════════════════════
    public ResponseDto<List<Envios>> listarEnvios(String correoUsuario, String rolUsuario,
                                                   String estado, String dniReceptor) {
        try {
            List<Envios> envios;
            boolean esAdminGeneral = rolUsuario != null && rolUsuario.contains("Administrador General");

            if (esAdminGeneral) {
                envios = enviosRepository.findAll();
            } else {
                Usuarios usuario = usuariosRepository.findByCorreo(correoUsuario).orElse(null);
                if (usuario != null && usuario.getSede() != null) {
                    envios = enviosRepository.findByOrigenOrDestino(
                            usuario.getSede(), usuario.getSede());
                } else {
                    envios = Collections.emptyList();
                }
            }

            if (estado != null && !estado.isBlank())
                envios = envios.stream()
                        .filter(e -> estado.equalsIgnoreCase(e.getEstado()))
                        .toList();
            if (dniReceptor != null && !dniReceptor.isBlank())
                envios = envios.stream()
                        .filter(e -> dniReceptor.equalsIgnoreCase(e.getReceptorDni()))
                        .toList();

            return new ResponseDto<>(200, "OK", envios);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════════════
    // MIS ENVÍOS (para clientes logueados)
    // ═══════════════════════════════════════════════
    public ResponseDto<List<Envios>> misEnvios(String correoUsuario) {
        try {
            Usuarios usuario = usuariosRepository.findByCorreo(correoUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            List<Envios> envios = enviosRepository.findByEmisor(usuario);
            return new ResponseDto<>(200, "OK", envios);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════════════
    // TRACKING PÚBLICO
    // ═══════════════════════════════════════════════
    public ResponseDto<Envios> buscarPorTracking(String codigo) {
        try {
            return enviosRepository.findByCodigoTracking(codigo)
                    .map(e -> new ResponseDto<>(200, "Envío encontrado", e))
                    .orElse(new ResponseDto<>(404, "No se encontró envío con ese código", null));
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════════════
    // ESTADOS VÁLIDOS PARA UN ENVÍO
    // ═══════════════════════════════════════════════
    public ResponseDto<List<String>> estadosPermitidos(UUID id, String rolUsuario) {
        try {
            Envios envio = enviosRepository.findById(id).orElse(null);
            if (envio == null) return new ResponseDto<>(404, "Envío no encontrado", null);

            Map<String, List<String>> transiciones = "DOMICILIO".equalsIgnoreCase(envio.getTipoEntrega())
                    ? TRANSICIONES_DOMICILIO : TRANSICIONES_SEDE;

            List<String> estados = new ArrayList<>(
                    transiciones.getOrDefault(envio.getEstado(), Collections.emptyList()));

            // Agregar Cancelado si es Admin General y el envío aún no salió
            boolean esAdminGeneral = rolUsuario != null && rolUsuario.contains("Administrador General");
            boolean puedeCancelar = !List.of(ST_EN_TRANSITO, ST_EN_SEDE_DESTINO,
                    ST_LISTO_RECOGER, ST_EN_REPARTO, ST_ENTREGADO, ST_CANCELADO)
                    .contains(envio.getEstado());
            if (esAdminGeneral && puedeCancelar && !estados.contains(ST_CANCELADO))
                estados.add(ST_CANCELADO);

            return new ResponseDto<>(200, "OK", estados);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════════════
    // HELPERS DE CORREO
    // ═══════════════════════════════════════════════
    private String buildCorreoConfirmacion(Envios e) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
              <div style="background:#1a56db;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#fff;margin:0">✅ Envío registrado</h2>
              </div>
              <div style="background:#f8fafc;padding:24px;border-radius:0 0 12px 12px">
                <p>Hola <strong>%s</strong>,</p>
                <p>Tu envío ha sido registrado exitosamente.</p>
                <table style="width:100%%;border-collapse:collapse;margin:16px 0">
                  <tr><td style="padding:8px;color:#64748b">Código tracking</td>
                      <td style="padding:8px;font-weight:700;color:#f59e0b">%s</td></tr>
                  <tr><td style="padding:8px;color:#64748b">Documento</td>
                      <td style="padding:8px">%s N° %s</td></tr>
                  <tr><td style="padding:8px;color:#64748b">Estado</td>
                      <td style="padding:8px">%s</td></tr>
                  <tr><td style="padding:8px;color:#64748b">Entrega estimada</td>
                      <td style="padding:8px">%s</td></tr>
                </table>
                <p style="color:#64748b;font-size:12px">Se adjunta tu %s como comprobante.</p>
                <p style="color:#94a3b8;font-size:11px">© 2025 Envios Todopais</p>
              </div>
            </div>
            """.formatted(
                e.getEmisorNombre(), e.getCodigoTracking(),
                e.getTipoDocumento(), e.getNumeroDocumento(),
                e.getEstado(), e.getFechaEstimada(), e.getTipoDocumento().toLowerCase());
    }

    private String buildCorreoEstado(Envios e) {
        String nota = (e.getNotaEstado() != null && !e.getNotaEstado().isBlank())
                ? "<p><strong>Nota:</strong> " + e.getNotaEstado() + "</p>" : "";
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
              <div style="background:#1e293b;padding:24px;border-radius:12px 12px 0 0">
                <h2 style="color:#f59e0b;margin:0">📦 Actualización de tu envío</h2>
              </div>
              <div style="background:#f8fafc;padding:24px;border-radius:0 0 12px 12px">
                <p>El estado de tu envío <strong>%s</strong> ha cambiado a:</p>
                <h3 style="color:#1a56db">%s</h3>
                %s
                <p style="color:#94a3b8;font-size:11px">© 2025 Envios Todopais</p>
              </div>
            </div>
            """.formatted(e.getCodigoTracking(), e.getEstado(), nota);
    }
}
