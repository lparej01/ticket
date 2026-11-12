package com.soporte.ticket.controller;

import com.soporte.ticket.mapper.TicketMapper;
import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.TicketRepository;
import com.soporte.ticket.dto.*;
import com.soporte.ticket.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tecnico")
@PreAuthorize("hasRole('TECNICO')") // Restringe todo el controlador a usuarios con rol ROLE_TECNICO
public class TecnicoController {

    private final TicketRepository ticketRepository;

    public TecnicoController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/tickets/todos")
    public ResponseEntity<List<TicketResponseDTO>> listarTodosLosTickets() {
        // Trae TODOS los tickets "ABIERTO" del sistema donde tecnico_id sea NULL
        List<Ticket> tickets = ticketRepository.findByEstadoAndTecnicoIsNullOrderByFechaCreacionDesc("ABIERTO");

        TicketMapper mapper = new TicketMapper();

        List<TicketResponseDTO> dtos = tickets.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 📋 Endpoint para ver los tickets asignados activos
    @GetMapping("/tickets/progreso")
    public ResponseEntity<List<Ticket>> listarTicketsEnProgreso(
            @AuthenticationPrincipal Usuario tecnicoAutenticado
    ) {
        List<Ticket> tickets = ticketRepository.findByTecnicoAndEstado(tecnicoAutenticado, "EN_PROGRESO");
        return ResponseEntity.ok(tickets);
    }

    // 🔒 Endpoint para cerrar un ticket asignado
    @PutMapping("/tickets/cerrar")
    public ResponseEntity<?> cerrarTicket(
            @RequestBody java.util.Map<String, String> payload,
            @AuthenticationPrincipal Usuario tecnicoAutenticado
    ) {
        if (!payload.containsKey("ticketId") || !payload.containsKey("resolucion")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Faltan los campos 'ticketId' o 'resolucion'"));
        }

        Long ticketId = Long.parseLong(payload.get("ticketId"));
        String resolucionTexto = payload.get("resolucion");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (ticket.getTecnico() == null || !ticket.getTecnico().getId().equals(tecnicoAutenticado.getId())) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "No tienes permiso para modificar este ticket"));
        }

        if ("CERRADO".equals(ticket.getEstado())) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Este ticket ya se encuentra CERRADO"));
        }

        ticket.setEstado("CERRADO");
        ticket.setResolucion(resolucionTexto);
        ticket.setFechaResolucion(LocalDateTime.now());
        ticketRepository.save(ticket);

        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Ticket cerrado con éxito",
                "ticketId", ticket.getId(),
                "estado", ticket.getEstado(),
                "resolucion", ticket.getResolucion()
        ));
    }
    // ✍️ Endpoint para que el técnico se autoasigne un ticket ABIERTO
    @PutMapping("/tickets/{ticketId}/asignar")
    public ResponseEntity<?> asignarTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Usuario tecnicoAutenticado // Extrae el Técnico directamente del JWT
    ) {
        // 1. Buscamos el ticket en la base de datos
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // 2. VALIDACIÓN: Asegurar que el ticket esté disponible (en estado ABIERTO)
        if (!"ABIERTO".equals(ticket.getEstado())) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "Este ticket ya está siendo atendido o ha sido cerrado por otro especialista."
            ));
        }

        // 3. Modificamos los datos asignando el técnico logueado y cambiando el estado
        ticket.setTecnico(tecnicoAutenticado);
        ticket.setEstado("EN_PROGRESO"); // Cambia de ABIERTO a EN_PROGRESO

        // 4. Guardamos los cambios en la base de datos
        ticketRepository.save(ticket);

        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "¡Te has asignado el ticket con éxito!",
                "ticketId", ticket.getId(),
                "nuevoEstado", ticket.getEstado()
        ));
    }

}
