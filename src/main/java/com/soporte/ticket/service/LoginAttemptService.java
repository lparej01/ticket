package com.soporte.ticket.service;



import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    final int MAX_INTENTOS = 5;
    final int MINUTOS_BLOQUEO = 15;

    // Estructuras en memoria para almanac intentos y tiempos de bloqueo por IP
    private final ConcurrentHashMap<String, Integer> intentosCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> bloqueoCache = new ConcurrentHashMap<>();

    // Registrar un intento fallido
    public void loginFallido(String ip) {
        if (isBloqueada(ip)) return;

        int intentos = intentosCache.getOrDefault(ip, 0) + 1;
        intentosCache.put(ip, intentos);

        if (intentos >= MAX_INTENTOS) {
            bloqueoCache.put(ip, LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            intentosCache.remove(ip); // Reiniciamos contador al bloquear
        }
    }

    // Registrar un login exitoso (limpia el historial de esa IP)
    public void loginExitoso(String ip) {
        intentosCache.remove(ip);
        bloqueoCache.remove(ip);
    }

    // Verificar si la IP se encuentra actualmente bloqueada
    public boolean isBloqueada(String ip) {
        if (!bloqueoCache.containsKey(ip)) {
            return false;
        }

        LocalDateTime tiempoDesbloqueo = bloqueoCache.get(ip);
        if (LocalDateTime.now().isAfter(tiempoDesbloqueo)) {
            bloqueoCache.remove(ip); // El tiempo ya pasó, desbloqueamos
            return false;
        }

        return true;
    }
}

