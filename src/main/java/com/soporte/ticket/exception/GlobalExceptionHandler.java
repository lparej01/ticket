package com.soporte.ticket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura errores cuando un recurso no existe (ej. Ticket no encontrado)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorRespuesta> manejarRuntimeException(RuntimeException ex) {
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Captura los errores de validación de los @NotBlank de tus DTOs automáticamente
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los datos enviados",
                LocalDateTime.now(),
                errores
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}

