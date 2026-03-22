package com.sanchez.Envios.Services;

import com.sanchez.Envios.Dto.ResponseDto;
import com.sanchez.Envios.Dto.UsuarioRequestDto;
import com.sanchez.Envios.Models.Roles;
import com.sanchez.Envios.Models.Tiendas;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Repositories.RolesRepository;
import com.sanchez.Envios.Repositories.TiendasRepository;
import com.sanchez.Envios.Repositories.UsuariosRepository;
import com.sanchez.Envios.Util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UsuariosService {

    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private TiendasRepository tiendasRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    // ═══════════════════════════════════════
    // LISTAR — filtrado por rol del solicitante
    // ═══════════════════════════════════════
    public ResponseDto<List<Usuarios>> listar(String correoSolicitante, String rolSolicitante,
                                               String filtroRol, Boolean filtroActivo) {
        try {
            List<Usuarios> usuarios;
            boolean esAdminGeneral = rolSolicitante != null &&
                    rolSolicitante.contains("Administrador General");

            if (esAdminGeneral) {
                // Admin General ve TODOS
                if (filtroRol != null && filtroActivo != null)
                    usuarios = usuariosRepository.findByRolNombreAndActivo(filtroRol, filtroActivo);
                else if (filtroRol != null)
                    usuarios = usuariosRepository.findByRolNombre(filtroRol);
                else if (filtroActivo != null)
                    usuarios = usuariosRepository.findByActivo(filtroActivo);
                else
                    usuarios = usuariosRepository.findAll();
            } else {
                // Admin de Sede ve solo su sede
                Usuarios solicitante = usuariosRepository.findByCorreo(correoSolicitante)
                        .orElseThrow(() -> new RuntimeException("Solicitante no encontrado"));
                Tiendas sede = solicitante.getSede();
                if (sede == null)
                    return new ResponseDto<>(400, "No tienes sede asignada", null);

                if (filtroActivo != null)
                    usuarios = usuariosRepository.findBySedeAndActivo(sede, filtroActivo);
                else
                    usuarios = usuariosRepository.findBySede(sede);
            }

            // Ocultar passwords
            usuarios.forEach(u -> u.setPassword(null));
            return new ResponseDto<>(200, "OK", usuarios);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════
    // CREAR USUARIO
    // ═══════════════════════════════════════
    public ResponseDto<Usuarios> crear(UsuarioRequestDto dto, String rolSolicitante) {
        try {
            // Validaciones
            if (dto.getCorreo() == null || dto.getPassword() == null || dto.getDni() == null)
                return new ResponseDto<>(400, "Correo, password y DNI son obligatorios", null);
            if (dto.getPassword().length() < 6)
                return new ResponseDto<>(400, "La contraseña debe tener al menos 6 caracteres", null);
            if (usuariosRepository.findByCorreo(dto.getCorreo()).isPresent())
                return new ResponseDto<>(409, "El correo ya está registrado", null);
            if (usuariosRepository.findByDni(dto.getDni()).isPresent())
                return new ResponseDto<>(409, "El DNI ya está registrado", null);

            // Rol
            String rolNombre = dto.getRolNombre() != null ? dto.getRolNombre() : "Cliente";

            // Admin de Sede solo puede crear Operadores y Clientes
            boolean esAdminGeneral = rolSolicitante != null &&
                    rolSolicitante.contains("Administrador General");
            if (!esAdminGeneral && (rolNombre.equals("Administrador General") ||
                    rolNombre.equals("Administrador de Sede")))
                return new ResponseDto<>(403,
                        "No tienes permiso para crear usuarios con ese rol", null);

            Roles rol = rolesRepository.findAll().stream()
                    .filter(r -> r.getNombre().equalsIgnoreCase(rolNombre))
                    .findFirst()
                    .orElse(null);
            if (rol == null)
                return new ResponseDto<>(400, "Rol no encontrado: " + rolNombre, null);

            // Sede
            Tiendas sede = null;
            if (dto.getSedeId() != null)
                sede = tiendasRepository.findById(dto.getSedeId()).orElse(null);

            Usuarios u = new Usuarios();
            u.setNombre(dto.getNombre());
            u.setApellidoP(dto.getApellidoP());
            u.setApellidoM(dto.getApellidoM() != null ? dto.getApellidoM() : "-");
            u.setCorreo(dto.getCorreo());
            u.setPassword(passwordEncoder.encode(dto.getPassword()));
            u.setDni(dto.getDni());
            u.setRol(rol);
            u.setSede(sede);
            u.setActivo(true);
            u.setFechaCreacion(LocalDateTime.now());

            Usuarios guardado = usuariosRepository.save(u);

            // Correo de bienvenida
            try {
                emailService.enviarCorreo(guardado.getCorreo(),
                        "Bienvenido a Envios Todopais",
                        """
                        <h2>¡Cuenta creada!</h2>
                        <p>Hola <strong>%s</strong>, tu cuenta ha sido creada.</p>
                        <p>Rol: <strong>%s</strong></p>
                        <p>Usa tu correo y la contraseña asignada para ingresar.</p>
                        """.formatted(guardado.getNombreCompleto(), rolNombre));
            } catch (Exception ignored) {}

            guardado.setPassword(null);
            return new ResponseDto<>(201, "Usuario creado correctamente", guardado);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════
    // EDITAR USUARIO
    // ═══════════════════════════════════════
    public ResponseDto<Usuarios> editar(UUID id, UsuarioRequestDto dto,
                                         String rolSolicitante) {
        try {
            Usuarios u = usuariosRepository.findById(id).orElse(null);
            if (u == null) return new ResponseDto<>(404, "Usuario no encontrado", null);

            boolean esAdminGeneral = rolSolicitante != null &&
                    rolSolicitante.contains("Administrador General");

            // Admin Sede no puede editar Admins Generales ni otros Admins Sede
            if (!esAdminGeneral) {
                String rolTarget = u.getRol().getNombre();
                if (rolTarget.equals("Administrador General") ||
                        rolTarget.equals("Administrador de Sede"))
                    return new ResponseDto<>(403,
                            "No tienes permiso para editar este usuario", null);
            }

            if (dto.getNombre()    != null) u.setNombre(dto.getNombre());
            if (dto.getApellidoP() != null) u.setApellidoP(dto.getApellidoP());
            if (dto.getApellidoM() != null) u.setApellidoM(dto.getApellidoM());
            if (dto.getActivo()    != null) u.setActivo(dto.getActivo());

            // Cambio de sede
            if (dto.getSedeId() != null) {
                Tiendas sede = tiendasRepository.findById(dto.getSedeId()).orElse(null);
                u.setSede(sede);
            }

            // Cambio de rol — solo Admin General
            if (dto.getRolNombre() != null && esAdminGeneral) {
                Roles rol = rolesRepository.findAll().stream()
                        .filter(r -> r.getNombre().equalsIgnoreCase(dto.getRolNombre()))
                        .findFirst().orElse(null);
                if (rol != null) u.setRol(rol);
            }

            u.setFechaActualizacion(LocalDateTime.now());
            Usuarios actualizado = usuariosRepository.save(u);
            actualizado.setPassword(null);
            return new ResponseDto<>(200, "Usuario actualizado", actualizado);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════
    // ACTIVAR / DESACTIVAR
    // ═══════════════════════════════════════
    public ResponseDto<Usuarios> toggleActivo(UUID id, String rolSolicitante) {
        try {
            Usuarios u = usuariosRepository.findById(id).orElse(null);
            if (u == null) return new ResponseDto<>(404, "Usuario no encontrado", null);

            boolean esAdminGeneral = rolSolicitante != null &&
                    rolSolicitante.contains("Administrador General");
            if (!esAdminGeneral) {
                String rolTarget = u.getRol().getNombre();
                if (rolTarget.equals("Administrador General") ||
                        rolTarget.equals("Administrador de Sede"))
                    return new ResponseDto<>(403,
                            "No tienes permiso para modificar este usuario", null);
            }

            u.setActivo(!Boolean.TRUE.equals(u.getActivo()));
            u.setFechaActualizacion(LocalDateTime.now());
            Usuarios updated = usuariosRepository.save(u);
            updated.setPassword(null);
            return new ResponseDto<>(200,
                    u.getActivo() ? "Usuario activado" : "Usuario desactivado", updated);

        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }

    // ═══════════════════════════════════════
    // ESTADÍSTICAS
    // ═══════════════════════════════════════
    public ResponseDto<Map<String, Object>> estadisticas(String correoSolicitante,
                                                          String rolSolicitante) {
        try {
            Map<String, Object> stats = new HashMap<>();
            boolean esAdminGeneral = rolSolicitante != null &&
                    rolSolicitante.contains("Administrador General");

            if (esAdminGeneral) {
                stats.put("total",     usuariosRepository.count());
                stats.put("activos",   usuariosRepository.countByActivo(true));
                stats.put("inactivos", usuariosRepository.countByActivo(false));
                // por rol
                for (String rol : List.of("Administrador General", "Administrador de Sede",
                        "Operador", "Cliente")) {
                    stats.put("rol_" + rol.toLowerCase().replace(" ", "_"),
                            usuariosRepository.findByRolNombre(rol).size());
                }
            } else {
                Usuarios solicitante = usuariosRepository.findByCorreo(correoSolicitante)
                        .orElseThrow();
                Tiendas sede = solicitante.getSede();
                if (sede != null) {
                    List<Usuarios> deSede = usuariosRepository.findBySede(sede);
                    stats.put("total",     deSede.size());
                    stats.put("activos",   deSede.stream().filter(u -> Boolean.TRUE.equals(u.getActivo())).count());
                    stats.put("inactivos", deSede.stream().filter(u -> !Boolean.TRUE.equals(u.getActivo())).count());
                    stats.put("sede",      sede.getNombre());
                }
            }
            return new ResponseDto<>(200, "OK", stats);
        } catch (Exception e) {
            return new ResponseDto<>(500, "Error: " + e.getMessage(), null);
        }
    }
}
