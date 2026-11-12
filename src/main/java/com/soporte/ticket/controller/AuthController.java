package com.soporte.ticket.controller;

import com.soporte.ticket.config.CustomUserDetailsService;
import com.soporte.ticket.config.JwtUtils;
import com.soporte.ticket.dto.AuthResponseDTO;
import com.soporte.ticket.dto.LoginRequestDTO;
import com.soporte.ticket.dto.RegistroRequestDTO;
import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.UserRepository;
import com.soporte.ticket.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistService tokenBlacklistService; // Inyecta el servicio de lista negra

    // Constructor actualizado con las nuevas dependencias de registro
    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtils jwtUtils,
                          UserRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ENDPOINT NUEVO: Registro de usuarios dinámico
    @PostMapping("/register")
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody RegistroRequestDTO request) {
        // 1. Check if the email already exists in PostgreSQL
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ResponseEntity<>("El correo electronico ya esta registrado", HttpStatus.BAD_REQUEST);
        }

        String rolInput = request.getRol() != null ? request.getRol().toUpperCase() : "";

        if (!rolInput.equals("CLIENTE") && !rolInput.equals("TECNICO") && !rolInput.equals("ADMIN")) {
            return new ResponseEntity<>("Rol inválido. Use: CLIENTE, TECNICO o ADMIN", HttpStatus.BAD_REQUEST);
        }

        String rolAsignado = "ROLE_" + rolInput;




        // 3.Create the entity and incorporate the encrypted password with BCrypt
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setRol(rolAsignado);

        // 4. Save to the database
        usuarioRepository.save(nuevoUsuario);

        return new ResponseEntity<>("Usuario registrado exitosamente", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String token = jwtUtils.generarToken(userDetails);
        //return ResponseEntity.ok(new AuthResponseDTO(token));
        return ResponseEntity.ok(new AuthResponseDTO(token, userDetails.getAuthorities().toString()));

    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> cerrarSesion(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Enviamos el token actual a la lista negra
            tokenBlacklistService.invalidarToken(token);

            return ResponseEntity.ok(Map.of("mensaje", "Cierre de sesión exitoso. El token ha sido revocado."));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "No se encontró un token válido en la cabecera de la petición."));
    }
}
