package com.soporte.ticket.service;

import com.soporte.ticket.dto.TicketRequestDTO;
import com.soporte.ticket.dto.TicketResponseDTO;
import com.soporte.ticket.model.Ticket;

import java.util.List;

public interface TicketService {
    TicketResponseDTO crearTicket(TicketRequestDTO dto, String clienteEmail);
    List<TicketResponseDTO> listarTodos();
    List<TicketResponseDTO> listarPorCliente(Long clienteId);
    TicketResponseDTO asignarTecnico(Long ticketId, Long tecnicoId);
    List<TicketResponseDTO> listarPorClienteEmail(String clienteEmail);
    List<Ticket> obtenerTicketsDisponibles();
}

