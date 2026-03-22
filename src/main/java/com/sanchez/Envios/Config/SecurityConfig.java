package com.sanchez.Envios.Config;

import com.sanchez.Envios.Security.CustomUserDetailsService;
import com.sanchez.Envios.Security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService uds;
    private final JwtAuthenticationFilter jwtFilter;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService uds,
                          JwtAuthenticationFilter jwtFilter,
                          PasswordEncoder passwordEncoder) {
        this.uds = uds;
        this.jwtFilter = jwtFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Públicos — sin autenticación
                .requestMatchers(
                    "/api/auth/**",
                    "/api/envios/tracking/**",
                    "/api/tiendas/listar",
                    "/api/tiendas/*",
                    "/api/ubigeo/**",
                    "/api/cotizador/calcular",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // Protegidos — cualquier usuario autenticado
                .requestMatchers(
                    "/api/envios/mis-envios",
                    "/api/auth/usuarios/perfil"
                ).authenticated()
                // Solo roles operativos
                .requestMatchers("/api/envios/crear").hasAnyRole(
                    "Operador", "Administrador de Sede", "Administrador General")
                .requestMatchers("/api/envios/listar").hasAnyRole(
                    "Operador", "Administrador de Sede", "Administrador General")
                .requestMatchers("/api/envios/*/estado").hasAnyRole(
                    "Operador", "Administrador de Sede", "Administrador General")
                .requestMatchers("/api/envios/*/estados-validos").hasAnyRole(
                    "Operador", "Administrador de Sede", "Administrador General")
                .requestMatchers("/api/envios/*/boleta").authenticated()
                // Admin de Sede y General: sedes y tarifas
                .requestMatchers("/api/tiendas/crear", "/api/tiendas/*/editar",
                                 "/api/tiendas/*").hasAnyRole(
                    "Administrador de Sede", "Administrador General")
                .requestMatchers("/api/cotizador/tarifas/**").hasAnyRole(
                    "Administrador de Sede", "Administrador General")
                // Usuarios — Admin General y Admin Sede
                .requestMatchers("/api/usuarios/**").hasAnyRole(
                    "Administrador General", "Administrador de Sede")
                // Reportes — Admin General y Admin Sede
                .requestMatchers("/api/reportes/**").hasAnyRole(
                    "Administrador General", "Administrador de Sede")
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
