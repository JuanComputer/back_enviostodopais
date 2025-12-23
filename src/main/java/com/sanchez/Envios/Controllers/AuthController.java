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

    private final UsuariosRepository usuariosRepository;
    private final RolesRepository rolesRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UsuariosRepository usuariosRepository,
                          RolesRepository rolesRepository,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuariosRepository = usuariosRepository;
        this.rolesRepository = rolesRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService=emailService;
    }

    @PostMapping("/register")
    public ResponseDto<Usuarios> register(@RequestBody RegisterRequest req) {
        try {
            // validaciones básicas
            if (Objects.isNull(req.getName()) ||
                    Objects.isNull(req.getApellidoP()) ||
                    Objects.isNull(req.getApellidoM()) ||
                    Objects.isNull(req.getCorreo()) ||
                    Objects.isNull(req.getPassword()) ||
                    Objects.isNull(req.getDni())) {
                return new ResponseDto<>(400, "correo, password y dni son obligatorios", null);
            }
            if (req.getPassword().length() < 6) {
                return new ResponseDto<>(400, "La contraseña debe tener al menos 6 caracteres", null);
            }

            // chequeo existencia por correo o dni
            boolean existeCorreo = usuariosRepository.findAll().stream()
                    .anyMatch(u -> u.getCorreo().equalsIgnoreCase(req.getCorreo()));
            if (existeCorreo) {
                return new ResponseDto<>(409, "Correo ya registrado", null);
            }

            boolean existeDni = usuariosRepository.findAll().stream()
                    .anyMatch(u -> u.getDni().equals(req.getDni()));
            if (existeDni) {
                return new ResponseDto<>(409, "DNI ya registrado", null);
            }

            Usuarios u = new Usuarios();
            u.setCorreo(req.getCorreo());
            u.setPassword(passwordEncoder.encode(req.getPassword()));
            u.setDni(req.getDni());
            u.setNombre(req.getName());
            u.setApellidoP(req.getApellidoP());
            u.setApellidoM(req.getApellidoM());
            u.setFechaCreacion(LocalDateTime.now());
            u.setActivo(true);

            // asignar rol por defecto "Cliente"
            Roles rol = null;
            if (req.getRolId() != null) {
                Optional<Roles> r = rolesRepository.findById(req.getRolId());
                if (r.isPresent()) rol = r.get();
            }
            if (rol == null) {
                rol = rolesRepository.findAll().stream()
                        .filter(r2 -> "Cliente".equalsIgnoreCase(r2.getNombre()))
                        .findFirst()
                        .orElse(null);
            }
            u.setRol(rol);

            Usuarios saved = usuariosRepository.save(u);

            // Enviar correo de bienvenida
            String asunto = "Bienvenido a nuestra plataforma NVIOS-TODOPAIS";
            String cuerpo = """
                    <h2>¡Registro exitoso!</h2>
                    <p>Hola %s, tu cuenta ha sido creada correctamente.</p>
                    <p>Podrás acceder al sistema con tu correo registrado.</p>
                    <br>
                    <small>© 2025 Sistema de Envíos</small>
                    """.formatted(saved.getNombre() + " " +saved.getApellidoP());

            emailService.enviarCorreo(saved.getCorreo(), asunto, cuerpo);


            return new ResponseDto<>(201, "Registrado correctamente", saved);

        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al registrar: " + ex.getMessage(), null);
        }
    }

    @PostMapping("/login")
    public ResponseDto<AuthResponse> login(@RequestBody LoginRequest req) {
        try {
            if (req.getCorreo() == null || req.getPassword() == null) {
                return new ResponseDto<>(400, "correo y password son obligatorios", null);
            }
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getCorreo(), req.getPassword())
            );
            // si llega aquí es correcto  carga usuario para obtener rol
            Usuarios u = usuariosRepository.findAll().stream()
                    .filter(x -> x.getCorreo().equalsIgnoreCase(req.getCorreo()))
                    .findFirst()
                    .orElseThrow();

            String role = u.getRol() != null ? u.getRol().getNombre() : "Cliente";
            String token = jwtUtil.generateToken(u.getCorreo(), role);

            AuthResponse ar = AuthResponse.builder().token(token).role(role).build();

            // opcional: guardar un registro en tb_sesiones (historial) si quieres, no guardamos el token por requerimiento

            return new ResponseDto<>(200, "Login correcto", ar);
        } catch (BadCredentialsException ex) {
            return new ResponseDto<>(401, "Credenciales incorrectas", null);
        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al autenticar: " + ex.getMessage(), null);
        }
    }

    @PostMapping("/reset-password")
    public ResponseDto<Void> resetPassword(@RequestBody PasswordResetRequest req) {
        try {
            if (req.getCorreo() == null || req.getDni() == null || req.getNuevaPassword() == null) {
                return new ResponseDto<>(400, "correo, dni y nuevaPassword son obligatorios", null);
            }
            if (req.getNuevaPassword().length() < 6) {
                return new ResponseDto<>(400, "La contraseña debe tener al menos 6 caracteres", null);
            }

            System.out.println("EL req: "+ req.toString());
            Optional<Usuarios> opt = usuariosRepository.findByDniAndCorreo(req.getDni(), req.getCorreo());

            if (opt.isEmpty()) {
                return new ResponseDto<>(404, "Usuario no encontrado con esos datos", null);
            }

            Usuarios u = opt.get();
            u.setPassword(passwordEncoder.encode(req.getNuevaPassword()));
            usuariosRepository.save(u);
            u.setFechaActualizacion(LocalDateTime.now());

            String asunto = "Reestablecimiento contraseña";
            String cuerpo = """
                    <h2>Restablecimiento de Contraseña</h2>
                    <p>Hola %s, tu contraseña ha sido actualizada correctamente.</p>
                    <p>Si no realizaste este cambio, contacta con soporte inmediatamente.</p>
                    """.formatted(u.getNombre() + " " +u.getApellidoP());

            emailService.enviarCorreo(u.getCorreo(), asunto, cuerpo);

            return new ResponseDto<>(200, "Contraseña actualizada correctamente", null);
        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al resetear contraseña: " + ex.getMessage(), null);
        }
    }
    @GetMapping("/usuarios/perfil")
    public ResponseDto<Usuarios> getUserProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return new ResponseDto<>(401, "No autenticado", null);
            }

            String correo = auth.getName(); // Obtiene el correo del token
            Usuarios user = usuariosRepository.findByCorreo(correo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return new ResponseDto<>(200, "Perfil obtenido", user);
        } catch (Exception ex) {
            return new ResponseDto<>(500, "Error al obtener perfil: " + ex.getMessage(), null);
        }
    }
}
