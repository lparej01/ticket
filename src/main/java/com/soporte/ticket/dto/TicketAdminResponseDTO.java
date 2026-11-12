package com.soporte.ticket.dto;


import java.time.LocalDateTime;

public class TicketAdminResponseDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String estado;
    private String prioridad;
    private String clienteEmail;
    private String tecnicoEmail;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
    private String tiempoDuracion; // Ej: "15 horas, 30 minutos"


}

