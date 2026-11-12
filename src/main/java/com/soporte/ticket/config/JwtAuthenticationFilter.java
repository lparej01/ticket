package com.soporte.ticket.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    // Inyección de dependencias por constructor
    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Validar que la cabecera HTTP contenga el prefijo estándar 'Bearer '
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token
        jwt = authHeader.substring(7);

        // ============================================================
        // ¡NUEVA VALIDACIÓN! Evita procesar strings malformados o nulos
        // ============================================================
        if (jwt.isBlank() || jwt.equals("null") || jwt.equals("undefined") || !jwt.contains(".")) {
            // El token no es válido, ignoramos la autenticación y permitimos que
            // Spring Security responda de forma nativa (con un 401 Unauthorized estructurado)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el email de forma segura una vez pre-validado el formato básico
        try {
            userEmail = jwtUtils.extraerEmail(jwt);
        } catch (Exception e) {
            // Si ocurre cualquier otra excepción de parseo imprevista, registramos el fallo sin tumbar el servidor
            logger.warn("No se pudo parsear el token JWT enviado: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Si el token tiene un email y no está autenticado en el contexto actual...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtUtils.esTokenValido(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                // Adjuntamos detalles adicionales de la petición web actual si es necesario
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros de Spring Security
        filterChain.doFilter(request, response);
    }

}
