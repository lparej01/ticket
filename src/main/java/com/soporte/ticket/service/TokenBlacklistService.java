package com.soporte.ticket.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Almacena los tokens invalidados en memoria de forma ultra rápida
    private final ConcurrentHashMap<String, Boolean> blacklistCache = new ConcurrentHashMap<>();

    // Agrega el token a la lista negra al cerrar sesión
    public void invalidarToken(String token) {
        if (token != null) {
            blacklistCache.put(token, true);
        }
    }

    // Verifica si el token ya se encuentra en la lista negra
    public boolean isTokenInvalidado(String token) {
        return blacklistCache.containsKey(token);
    }
}

