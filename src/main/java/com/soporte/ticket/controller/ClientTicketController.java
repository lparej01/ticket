package com.soporte.ticket.controller;



import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.TicketRepository;
import com.soporte.ticket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cliente/tickets")
public class ClientTicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Crear un nuevo ticket de soporte
    @PostMapping
    public ResponseEntity<?> crearTicket(@RequestBody Ticket nuevoTicket) {
        // Extrae el email del token JWT de la sesión actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailCliente = auth.getName();

        // Busca el usuario en la base de datos
        Usuario cliente = userRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado en el sistema"));

        // Vincula el cliente al ticket y guarda
        nuevoTicket.setCliente(cliente);
        Ticket ticketGuardado = ticketRepository.save(nuevoTicket);

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketGuardado);
    }

    // 2. Consultar el historial de tickets del propio cliente logueado
    @GetMapping
    public ResponseEntity<List<Ticket>> listarMisTickets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailCliente = auth.getName();

        Usuario cliente = userRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        return ResponseEntity.ok(ticketRepository.findByCliente(cliente));
    }
}
