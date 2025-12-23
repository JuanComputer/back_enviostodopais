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
        Usuarios u = usuariosRepository.findAll().stream()
                .filter(x -> x.getCorreo().equalsIgnoreCase(correo))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String roleName = u.getRol() != null ? u.getRol().getNombre() : "Cliente";

        return new User(u.getCorreo(), u.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase())));
    }
}
