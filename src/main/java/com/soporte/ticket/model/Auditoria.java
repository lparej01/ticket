package com.soporte.ticket.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;       // Quién lo hizo (extraído del token)
    private String operacion;     // Qué hizo (ej: "CREAR_TICKET", "LOGIN")
    private String metodoHttp;    // POST, PUT, DELETE, GET
    private String url;           // El endpoint afectado
    private String ip;            // Dirección IP del cliente
    private LocalDateTime fecha;  // Cuándo lo hizo
}

