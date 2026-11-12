package com.soporte.ticket.mapper;

import com.soporte.ticket.dto.TicketResponseDTO;
import com.soporte.ticket.model.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponseDTO toDto(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTitulo(ticket.getTitulo());
        dto.setDescripcion(ticket.getDescripcion());
        dto.setEstado(String.valueOf(ticket.getEstado()));
        dto.setFechaCreacion(ticket.getFechaCreacion());
        dto.setPrioridad(ticket.getPrioridad());

        // Extraemos solo el email de las relaciones para proteger la privacidad del usuario
        if (ticket.getCliente() != null) {
            dto.setClienteEmail(ticket.getCliente().getEmail());
        }

        if (ticket.getTecnico() != null) {
            dto.setTecnicoEmail(ticket.getTecnico().getEmail());
        }

        return dto;
    }
}

