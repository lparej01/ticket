package com.soporte.ticket.controller;

import com.soporte.ticket.dto.TicketResponseDTO;
import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.TicketRepository;
import com.soporte.ticket.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:4200") // Permite la conexión desde Angular
public class TicketController {

    private final TicketRepository ticketRepository;

    private final TicketService ticketService;

    public TicketController(TicketRepository ticketRepository, TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
    }

    @PostMapping("/crear")
    public ResponseEntity<Ticket> crearTicket(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Usuario clienteAutenticado) {
        Ticket ticket = new Ticket();
        ticket.setTitulo(body.get("titulo"));
        ticket.setDescripcion(body.get("descripcion"));
        ticket.setEstado("ABIERTO");
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setCliente(clienteAutenticado);
        String prioridadInput = body.getOrDefault("prioridad", "MEDIA").toUpperCase();
        ticket.setPrioridad(prioridadInput);

        Ticket ticketGuardado = ticketRepository.save(ticket);
        return new ResponseEntity<>(ticketGuardado, HttpStatus.CREATED);
    }
    @GetMapping("/mis-tickets")
    public ResponseEntity<List<TicketResponseDTO>> listarMisTickets(Authentication authentication) {
        // 1. Extrae de forma segura el email del cliente autenticado desde el JWT
        String clienteEmail = authentication.getName();


        List<TicketResponseDTO> tickets = ticketService.listarPorClienteEmail(clienteEmail);


        return ResponseEntity.ok(tickets);
    }


}

