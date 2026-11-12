package com.soporte.ticket.config;

import com.soporte.ticket.model.Auditoria;
import com.soporte.ticket.repository.AuditoriaRepository;
import com.soporte.ticket.service.LoginAttemptService;
import com.soporte.ticket.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;


@Component
public class AuditFilter extends OncePerRequestFilter {

    @Autowired
    private AuditoriaRepository auditRepository;
    @Autowired
    private LoginAttemptService loginAttemptService; // <-- INYECTA EL NUEVO SERVICIO
    @Autowired
    private TokenBlacklistService tokenBlacklistService; // <-- INYECTA EL NUEVO SERVICIO

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String url = request.getRequestURI();

        // 1. Control de IP Bloqueada (Módulo anterior)
        if (url.contains("/login") && loginAttemptService.isBloqueada(ip)) {
            response.setStatus(423);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Demasiados intentos. Tu IP ha sido bloqueada por 15 minutos.\"}");
            registrarBloqueoEnAuditoria(request);
            return;
        }

        // 2. CONTROL DE SEGURIDAD JWT: Extraer y verificar si el token está en la lista negra
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remueve la palabra "Bearer "

            if (tokenBlacklistService.isTokenInvalidado(token)) {
                response.setStatus(401); // HTTP 401 Unauthorized
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Sesión inválida. Este token ya fue destruido al cerrar sesión.\"}");
                return; // Corta la ejecución de la petición aquí mismo
            }
        }

        // 3. Si todo está en orden, la petición continúa su flujo
        filterChain.doFilter(request, response);
    }
    private void registrarBloqueoEnAuditoria(HttpServletRequest request) {
        Auditoria audit = new Auditoria();
        audit.setUsuario("ANÓNIMO (SOPRECHOSO)");
        audit.setMetodoHttp(request.getMethod());
        audit.setUrl(request.getRequestURI());
        audit.setIp(request.getRemoteAddr());
        audit.setOperacion("INICIO_SESION - IP_BLOQUEADA_FUERZA_BRUTA");
        audit.setFecha(LocalDateTime.now());
        auditRepository.save(audit);
    }

    // Método auxiliar para no repetir código de guardado
    private void guardarRegistroAuditoria(HttpServletRequest request, String prefijoOperacion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANÓNIMO";

        Auditoria audit = new Auditoria();
        audit.setUsuario(username);
        audit.setMetodoHttp(request.getMethod());
        audit.setUrl(request.getRequestURI());
        audit.setIp(request.getRemoteAddr());

        // Ej: "INICIO_SESION - ACCESO_DENEGADO_O_FALLIDO" o "CREACIÓN - ÉXITO"
        String operacionBase = determinarOperacion(request.getMethod(), request.getRequestURI());
        audit.setOperacion(operacionBase + " - " + prefijoOperacion);

        audit.setFecha(LocalDateTime.now());
        auditRepository.save(audit);
    }

    private String determinarOperacion(String metodo, String url) {
        if (url.contains("/login")) return "INICIO_SESION";
        switch (metodo) {
            case "POST" -> {
                return "CREACIÓN";
            }
            case "PUT" -> {
                return "MODIFICACIÓN";
            }
            case "DELETE" -> {
                return "ELIMINACIÓN";
            }
            default -> {
                return "CONSULTA";
            }
        }
    }
}

