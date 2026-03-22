package com.sanchez.Envios.Security;

import com.sanchez.Envios.Models.Usuarios;
import com.sanchez.Envios.Repositories.UsuariosRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuariosRepository usuariosRepository;

    public CustomUserDetailsService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // Fix: usar findByCorreo en lugar de findAll() + filter
        Usuarios u = usuariosRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        // Verificar que la cuenta esté activa
        if (!Boolean.TRUE.equals(u.getActivo())) {
            throw new UsernameNotFoundException("Cuenta desactivada: " + correo);
        }

        // Fix: usar el nombre original del rol SIN toUpperCase
        // para que coincida con hasRole() en SecurityConfig
        String roleName = u.getRol() != null ? u.getRol().getNombre() : "Cliente";

        return new User(
                u.getCorreo(),
                u.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + roleName)
                )
        );
    }
}
