package com.soporte.ticket.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {



    private final AuditFilter auditFilter;
    private final JwtAuthenticationFilter jwtAuthFilter;




    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuditFilter auditFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.auditFilter = auditFilter;
    }


    // 1. Aquí definimos el Bean que le falta a tu DataInitializer
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(); // Encriptador estándar empresarial
    }

    // 2. Configuración básica de los accesos a la API
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. NUEVO: Activar la configuración CORS antes de deshabilitar CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 1. Deshabilitar CSRF (API sin estado)
                .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                // 2. Definir los niveles de acceso por rol y ruta
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Login y registro públicos

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 🔥 NUEVA REGLA: Protege el endpoint del equipo de soporte
                        .requestMatchers("/api/tecnico/**").hasRole("TECNICO")

                        //.requestMatchers("/api/tickets/**").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers("/api/tickets/**").authenticated()

                        .requestMatchers("/api/cliente/**").hasAnyRole("CLIENTE", "ADMIN")

                        .anyRequest().authenticated()
                )

                // 3. Desactivar el manejo de sesiones clásico
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4.
        // PRIMERO: Extraemos el usuario del token y lo autenticamos formalmente
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // SEGUNDO: El filtro de auditoría procesa la IP, la lista negra y guarda el registro
        http.addFilterBefore(auditFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    // ===================================================================
    // EL BEAN FALTANTE (Agrega este método exactamente así)
    // ===================================================================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public CacheManager cacheManager() {
        // Declara un gestor de caché simple basado en memoria RAM
        return new ConcurrentMapCacheManager("loginAttempts", "tokens");
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // URL por defecto de Angular
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todos los endpoints
        return source;
    }
}

