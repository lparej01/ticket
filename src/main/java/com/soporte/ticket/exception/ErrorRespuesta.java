package com.soporte.ticket.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorRespuesta {
    private int codigo;
    private String mensaje;
    private LocalDateTime fecha;
    private Map<String, String> validaciones; // Para capturar qué campos fallaron en los DTOs
}
