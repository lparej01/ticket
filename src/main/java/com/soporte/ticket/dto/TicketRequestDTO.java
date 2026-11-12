package com.soporte.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDTO {

    @NotBlank(message = "El titulo no puede estar vacio")
    private String titulo;

    @NotBlank(message = "La descripcion no puede estar vacia")
    private String descripcion;

    private String prioridad; // BAJA, MEDIA, ALTA
}

