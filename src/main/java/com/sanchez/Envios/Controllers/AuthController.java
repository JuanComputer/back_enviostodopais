package com.sanchez.Envios.Controllers;

import com.sanchez.Envios.Dto.*;
import com.sanchez.Envios.Models.Roles;
import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Repositories.RolesRepository;
import com.sanchez.Envios.Repositories.UsuariosRepository;
import com.sanchez.Envios.Security.JwtUtil;
import com.sanchez.Envios.Util.EmailService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UsuariosRepository    usuariosRepository;
    private final RolesRepository       rolesRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;
    private final PasswordEncoder       passwordEncoder;
    private final EmailService          emailService;

    public AuthController(UsuariosRepository usuariosRepository,
                          RolesRepository rolesRepository,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuariosRepository    = usuariosRepository;
        this.rolesRepository       = rolesRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil               = jwtUtil;
        this.passwordEncoder       = passwordEncoder;
        this.emailService          = emailService;
    }

    // ══════════════════════════════════════
    // REGISTRO
    // ══════════════════════════════════════
    @PostMapping("/register")
    public ResponseDto<Usuarios> register(@RequestBody RegisterRequest req) {
        try {
            if (Objects.isNull(req.getName())     || Objects.isNull(req.getApellidoP()) ||
                Objects.isNull(req.getApellidoM()) || Objects.isNull(req.getCorreo())   ||
                Objects.isNull(req.getPassword())  || Objects.isNull(req.getDni())) {
                return new ResponseDto<>(400, "Todos los campos son obligatorios", null);
            }
            if (req.getPassword().length() < 6)
                return new ResponseDto<>(400, "La contraseña debe tener al menos 6 caracteres", null);

            // Fix: usar findByCorreo en lugar de findAll()
            if (usuariosRepository.findByCorreo(req.getCorreo()).isPresent())
                return new ResponseDto<>(409, "Correo ya registrado", null);
            if (usuariosRepository.findByDni(req.getDni()).isPresent())
                return new ResponseDto<>(409, "DNI ya registrado", null);

            // Rol por defecto: Cliente
            Roles rol = null;
            if (req.getRolId() != null)
                rol = rolesRepository.findById(req.getRolId()).orElse(null);
            if (rol == null)
                rol = rolesRepository.findByNombre("Cliente").orElse(null);

            Usuarios u = new Usuarios();
            u.setCorreo(req.getCorreo());
            u.setPassword(passwordEncoder.encode(req.getPassword()));
            u.setDni(req.getDni());
            u.setNombre(req.getName());
            u.setApellidoP(req.getApellidoP());
            u.setApellidoM(req.getApellidoM());
            u.setRol(rol);
            u.setActivo(true);
            u.setFechaCreacion(LocalDateTime.now());

            Usuarios saved = usuariosRepository.save(u);

            try {
                emailService.enviarCorreo(
                    saved.getCorreo(),
                    "Bienvenido a Envios Todopais",
                    """
                    <h2>¡Registro exitoso!</h2>
                    <p>Hola <strong>%s</strong>, tu cuenta ha sido creada correctamente.</p>
                    <p>Ya puedes acceder al sistema con tu correo y contraseña.</p>
                    <br><small>© 2025 Envios Todopais</small>
                    """.formatted(saved.getNombre() + " " + saved.getApellidoP())
                );
            } catch (Exception ignored) {}

            saved.setPassword(null);
            return new ResponseDto<>(201, "Registrado correctamente", saved);

        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al registrar: " + ex.getMessage(), null);
        }
    }

    // ══════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════
    @PostMapping("/login")
    public ResponseDto<AuthResponse> login(@RequestBody LoginRequest req) {
        try {
            if (req.getCorreo() == null || req.getPassword() == null)
                return new ResponseDto<>(400, "Correo y password son obligatorios", null);

            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getCorreo(), req.getPassword())
            );

            // Fix: usar findByCorreo en lugar de findAll()
            Usuarios u = usuariosRepository.findByCorreo(req.getCorreo())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!Boolean.TRUE.equals(u.getActivo()))
                return new ResponseDto<>(403, "Cuenta desactivada. Contacta al administrador.", null);

            String role = u.getRol() != null ? u.getRol().getNombre() : "Cliente";
            String token = jwtUtil.generateToken(u.getCorreo(), role);

            AuthResponse ar = AuthResponse.builder()
                    .token(token)
                    .role(role)
                    .build();

            return new ResponseDto<>(200, "Login correcto", ar);

        } catch (BadCredentialsException ex) {
            return new ResponseDto<>(401, "Credenciales incorrectas", null);
        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al autenticar: " + ex.getMessage(), null);
        }
    }

    // ══════════════════════════════════════
    // RESET PASSWORD
    // ══════════════════════════════════════
    @PostMapping("/reset-password")
    public ResponseDto<Void> resetPassword(@RequestBody PasswordResetRequest req) {
        try {
            if (req.getCorreo() == null || req.getDni() == null || req.getNuevaPassword() == null)
                return new ResponseDto<>(400, "Correo, DNI y nueva contraseña son obligatorios", null);
            if (req.getNuevaPassword().length() < 6)
                return new ResponseDto<>(400, "La contraseña debe tener al menos 6 caracteres", null);

            Usuarios u = usuariosRepository.findByDniAndCorreo(req.getDni(), req.getCorreo())
                    .orElse(null);
            if (u == null)
                return new ResponseDto<>(404, "No se encontró usuario con esos datos", null);

            u.setPassword(passwordEncoder.encode(req.getNuevaPassword()));
            u.setFechaActualizacion(LocalDateTime.now());
            usuariosRepository.save(u);

            try {
                emailService.enviarCorreo(
                    u.getCorreo(),
                    "Contraseña restablecida — Envios Todopais",
                    """
                    <h2>Contraseña actualizada</h2>
                    <p>Hola <strong>%s</strong>, tu contraseña fue restablecida correctamente.</p>
                    <p>Si no realizaste este cambio, contacta con soporte inmediatamente.</p>
                    """.formatted(u.getNombre() + " " + u.getApellidoP())
                );
            } catch (Exception ignored) {}

            return new ResponseDto<>(200, "Contraseña actualizada correctamente", null);

        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al resetear contraseña: " + ex.getMessage(), null);
        }
    }

    // ══════════════════════════════════════
    // PERFIL
    // ══════════════════════════════════════
    @GetMapping("/usuarios/perfil")
    public ResponseDto<Usuarios> getUserProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated())
                return new ResponseDto<>(401, "No autenticado", null);

            Usuarios user = usuariosRepository.findByCorreo(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setPassword(null);
            return new ResponseDto<>(200, "Perfil obtenido", user);

        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al obtener perfil: " + ex.getMessage(), null);
        }
    }
}
