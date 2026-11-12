package com.soporte.ticket.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;




@Component
public class JwtUtils {

    // Inyecta la clave secreta desde el archivo application.yml
    @Value("${jwt.secret}")
    private String secretKey;

    // Inyecta el tiempo de expiracion
    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    // 1. GENERAR TOKEN: Crea el JWT usando el correo del usuario autenticado
    public String generarToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // Guardamos el email como el "subject"
                .issuedAt(new Date()) // Fecha de creacion
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Fecha de expiracion
                .signWith(obtenerClaveFirma()) // Firmamos con la clave secreta encriptada
                .compact();
    }

    // 2. OBTENER EMAIL: Extrae el correo electrónico contenido dentro del token
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // 3. VALIDAR TOKEN: Verifica si el token pertenece al usuario y no ha expirado
    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String email = extraerEmail(token);
        return (email.equals(userDetails.getUsername()) && !esTokenExpirado(token));
    }

    // --- METODOS INTERNOS AUXILIARES DE ENCRIPCION Y LECTURA ---

    // Transforma la cadena Base64 del archivo yml en una clave criptografica segura HMAC-SHA
    // Reemplaza el método viejo por este:
    private SecretKey obtenerClaveFirma() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // hmacShaKeyFor es el método estándar, Segura y compatible de JJWT 0.12+
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extrae cualquier dato específico (Claim) de los metadatos del token
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    // Lee y desencripta el cuerpo completo del JWT usando nuestra clave de firma
    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveFirma())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Comprueba si la fecha de expiracion del token ya pasó de la hora actual
    private boolean esTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }
}

