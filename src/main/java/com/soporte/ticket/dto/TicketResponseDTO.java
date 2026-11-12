package com.soporte.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String estado;
    private String prioridad;
    private LocalDateTime fechaCreacion;
    private String clienteEmail;
    private String tecnicoEmail; // Puede ser nulo si no se ha asignado técnico
}
