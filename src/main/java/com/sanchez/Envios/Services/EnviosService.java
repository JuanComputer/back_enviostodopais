package com.sanchez.Envios.Services;

import com.sanchez.Envios.Dto.EnvioRequestDto;
import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Models.Envios;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Repositories.EnviosRepository;
import com.sanchez.Envios.Repositories.TiendasRepository;
import com.sanchez.Envios.Repositories.UsuariosRepository;
import com.sanchez.Envios.Util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnviosService {

    @Autowired
    private EnviosRepository enviosRepository;

    @Autowired
    private TiendasRepository tiendasRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private EmailService emailService;

    // =====================================================
    // ✅ Crear un envío (con o sin emisor registrado)
    // =====================================================
    public ResponseDto<Envios> crearEnvio( EnvioRequestDto envioRequestDto
    ) {
        try {
            Tiendas destino = null;
            if (envioRequestDto.getDestinoId() != null) {
                destino = tiendasRepository.findById(envioRequestDto.getDestinoId()).orElse(null);
            }

            if (destino == null) {
                System.out.println("⚠️ No se encontró tienda destino. El envío quedará sin destino asignado.");
            }
            Usuarios emisor = null;
            if (envioRequestDto.getEmisorId() != null) {
                emisor = usuariosRepository.findById(envioRequestDto.getEmisorId()).orElse(null);
            }

            Envios envio = new Envios();
            envio.setCodigoTracking("PKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
//            "PKG-CC39f747

           envio.setFechaCreacion(LocalDateTime.now());

            envio.setEstado("En tránsito");
            envio.setDestino(destino);
            envio.setReceptorNombre(envioRequestDto.getReceptorNombre());
            envio.setReceptorDni(envioRequestDto.getReceptorDni());
            envio.setFechaEstimada(envioRequestDto.getFechaEstimada() != null ? envioRequestDto.getFechaEstimada() : LocalDate.now().plusDays(3));

            // Si el emisor existe en sistema
            if (emisor != null) {
                envio.setEmisor(emisor);
                envio.setEmisorNombre(emisor.getNombre() + " " + emisor.getApellidoP());
                envio.setEmisorDni(emisor.getDni());
                envio.setEmisorTelefono("Desconocido");
                envio.setEmisorCorreo(emisor.getCorreo());
            } else {
                // Emisor no registrado
                envio.setEmisorNombre(envioRequestDto.getEmisorNombre());
                envio.setEmisorDni(envioRequestDto.getEmisorDni());
                envio.setEmisorTelefono(envioRequestDto.getEmisorTelefono());
                envio.setEmisorCorreo(envioRequestDto.getEmisorCorreo()); // se puede extender a parámetro
            }

            Envios guardado = enviosRepository.save(envio);

            // Enviar correo de confirmación (si tiene correo)
            if (guardado.getEmisorCorreo() != null && !guardado.getEmisorCorreo().isBlank()) {
                String cuerpo = """
                        <h3>Estimado(a) %s,</h3>
                        <p>Su envío fue registrado correctamente.</p>
                        <p><b>Código de tracking:</b> %s</p>
                        <p><b>Estado actual:</b> %s</p>
                        <p>Fecha estimada de entrega: %s</p>
                        <hr>
                        <p>Gracias por confiar en nuestro servicio.</p>
                        """.formatted(
                        guardado.getEmisorNombre(),
                        guardado.getCodigoTracking(),
                        guardado.getEstado(),
                        guardado.getFechaEstimada()
                );

                emailService.enviarCorreo(
                        guardado.getEmisorCorreo(),
                        "Confirmación de registro de envío",
                        cuerpo
                );
            }

            return new ResponseDto<>(200, "Envío creado correctamente", guardado);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al crear el envío: " + e.getMessage(), null);
        }
    }

    // =====================================================
    // ✅ Listar envíos (con filtros opcionales)
    // =====================================================
    public ResponseDto<List<Envios>> listarEnvios(
            String estado,
            String dniReceptor,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        try {
            List<Envios> envios = enviosRepository.findAll();

            if (estado != null)
                envios.removeIf(e -> !e.getEstado().equalsIgnoreCase(estado));

            if (dniReceptor != null)
                envios.removeIf(e -> !e.getReceptorDni().equalsIgnoreCase(dniReceptor));

            if (fechaInicio != null)
                envios.removeIf(e -> e.getFechaCreacion().toLocalDate().isBefore(fechaInicio));

            if (fechaFin != null)
                envios.removeIf(e -> e.getFechaCreacion().toLocalDate().isAfter(fechaFin));

            return new ResponseDto<>(200, "Lista de envíos obtenida correctamente", envios);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al listar envíos: " + e.getMessage(), null);
        }
    }

    // =====================================================
    // ✅ Buscar por código tracking
    // =====================================================
    public ResponseDto<Envios> buscarPorTracking(String codigo) {
        try {
            Optional<Envios> envio = enviosRepository.findByCodigoTracking(codigo);
            if (envio.isEmpty())
                return new ResponseDto<>(404, "No se encontró envío con ese código", null);

            return new ResponseDto<>(200, "Envío encontrado", envio.get());

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al buscar envío: " + e.getMessage(), null);
        }
    }

    // =====================================================
    // ✅ Cambiar estado (notifica al emisor)
    // =====================================================
    public ResponseDto<Envios> cambiarEstado(UUID id, String nuevoEstado) {
        try {
            Optional<Envios> envioOpt = enviosRepository.findById(id);
            if (envioOpt.isEmpty())
                return new ResponseDto<>(404, "Envío no encontrado", null);

            Envios envio = envioOpt.get();
            envio.setEstado(nuevoEstado);
            envio.setFechaActualizacion(LocalDateTime.now());
            Envios actualizado = enviosRepository.save(envio);

            // Enviar correo de actualización
            if (envio.getEmisorCorreo() != null && !envio.getEmisorCorreo().isBlank()) {
                String cuerpo = """
                        <h3>Actualización de estado del envío</h3>
                        <p>El estado de su envío con código <b>%s</b> ha cambiado a:</p>
                        <p><b>%s</b></p>
                        <hr>
                        <p>Gracias por confiar en nosotros.</p>
                        """.formatted(envio.getCodigoTracking(), nuevoEstado);

                emailService.enviarCorreo(
                        envio.getEmisorCorreo(),
                        "Actualización de estado del envío",
                        cuerpo
                );
            }

            return new ResponseDto<>(200, "Estado actualizado correctamente", actualizado);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al cambiar estado: " + e.getMessage(), null);
        }
    }

    // =====================================================
    // ✅ Editar datos del envío
    // =====================================================
    public ResponseDto<Envios> editarEnvio(UUID id, String receptorNombre, String receptorDni, UUID destinoId) {
        try {
            Optional<Envios> envioOpt = enviosRepository.findById(id);
            if (envioOpt.isEmpty())
                return new ResponseDto<>(404, "Envío no encontrado", null);

            Envios envio = envioOpt.get();

            if (receptorNombre != null) envio.setReceptorNombre(receptorNombre);
            if (receptorDni != null) envio.setReceptorDni(receptorDni);
            if (destinoId != null) {
                Tiendas destino = tiendasRepository.findById(destinoId).orElse(null);
                if (destino != null) envio.setDestino(destino);
            }

            envio.setFechaActualizacion(LocalDateTime.now());
            Envios actualizado = enviosRepository.save(envio);

            return new ResponseDto<>(200, "Datos de envío actualizados", actualizado);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al editar envío: " + e.getMessage(), null);
        }
    }

    // =====================================================
    // ✅ Eliminar envío
    // =====================================================
    public ResponseDto<String> eliminarEnvio(UUID id) {
        try {
            if (!enviosRepository.existsById(id))
                return new ResponseDto<>(404, "El envío no existe", null);

            enviosRepository.deleteById(id);
            return new ResponseDto<>(200, "Envío eliminado correctamente", "OK");
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error al eliminar envío: " + e.getMessage(), null);
        }
    }
}
