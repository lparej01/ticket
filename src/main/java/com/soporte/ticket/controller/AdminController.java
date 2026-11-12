package com.soporte.ticket.controller;

import com.soporte.ticket.dto.UserDTO;
import com.soporte.ticket.model.Auditoria;
import com.soporte.ticket.repository.AuditoriaRepository;
import com.soporte.ticket.repository.UserRepository;
import com.soporte.ticket.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditoriaRepository auditRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private TicketRepository ticketRepository; //


    @GetMapping("/usuarios")
    public ResponseEntity<List<UserDTO>> listarUsuarios() {
        List<UserDTO> usuariosDto = userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getRol()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuariosDto);
    }


    @GetMapping("/auditoria")
    public ResponseEntity<List<Auditoria>> obtenerAuditoria() {
        return ResponseEntity.ok(auditRepository.findAllByOrderByFechaDesc());
    }


    @GetMapping("/auditoria/usuario/{username}")
    public ResponseEntity<List<Auditoria>> obtenerAuditoriaPorUsuario(@PathVariable String username) {
        return ResponseEntity.ok(auditRepository.findByUsuarioOrderByFechaDesc(username));
    }

    // 4. Desbloquear manualmente una dirección IP sospechosa
    @PostMapping("/auditoria/desbloquear-ip")
    public ResponseEntity<Map<String, String>> desbloquearIp(@RequestBody Map<String, String> request) {
        String ipParaDesbloquear = request.get("ip");

        if (ipParaDesbloquear == null || ipParaDesbloquear.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debes proporcionar el campo 'ip' en el cuerpo de la petición."));
        }

        loginAttemptService.loginExitoso(ipParaDesbloquear);

        return ResponseEntity.ok(Map.of("mensaje", "La IP " + ipParaDesbloquear + " ha sido desbloqueada exitosamente."));
    }


    @PutMapping("/tickets/asignar")
    public ResponseEntity<?> asignarTicket(@RequestBody Map<String, Long> payload) {
        Long ticketId = payload.get("ticketId");
        Long tecnicoId = payload.get("tecnicoId");


        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));


        Usuario tecnico = userRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));


        if (!"ROLE_TECNICO".equals(tecnico.getRol())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario seleccionado no tiene el rol de TECNICO"));
        }


        // Asignar el técnico al ticket
        ticket.setTecnico(tecnico);
        ticket.setEstado("EN_PROGRESO");
        ticketRepository.save(ticket);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Ticket asignado con éxito",
                "ticketId", ticket.getId(),
                "estado", ticket.getEstado(),
                "tecnicoAsignado", tecnico.getEmail()
        ));
    }
    // 📊 5. Endpoint para el Dashboard del Administrador con Tiempos de Resolución
    @GetMapping("/tickets/dashboard")
    public ResponseEntity<List<java.util.Map<String, Object>>> obtenerDashboardGlobal() {

        List<Ticket> tickets = ticketRepository.findSubremeAllTicketsNativo();

        // Imprime en la consola de Java cuántos tickets físicos existen realmente en tu BD
        System.out.println("--- TOTAL DE TICKETS EN BASE DE DATOS: " + tickets.size() + " ---");

        List<java.util.Map<String, Object>> respuesta = tickets.stream().map(t -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", t.getId());
            map.put("titulo", t.getTitulo());
            map.put("descripcion", t.getDescripcion());
            map.put("estado", t.getEstado());
            map.put("prioridad", t.getPrioridad());
            map.put("fechaCreacion", t.getFechaCreacion());

            if (t.getCliente() != null) {
                map.put("clienteEmail", t.getCliente().getEmail());
            } else {
                map.put("clienteEmail", "Anónimo / Sistema");
            }

            if (t.getTecnico() != null) {
                map.put("tecnicoEmail", t.getTecnico().getEmail());
            } else {
                map.put("tecnicoEmail", "Sin asignar");
            }

            if (t.getFechaCreacion() != null) {
                java.time.LocalDateTime horaFin = "CERRADO".equals(t.getEstado()) && t.getFechaResolucion() != null
                        ? t.getFechaResolucion()
                        : java.time.LocalDateTime.now();

                java.time.Duration duracion = java.time.Duration.between(t.getFechaCreacion(), horaFin);
                long dias = duracion.toDays();
                long horas = duracion.toHours() % 24;
                long minutos = duracion.toMinutes() % 60;

                if (dias > 0) {
                    map.put("tiempoDuracion", dias + "d " + horas + "h " + minutos + "m");
                } else if (horas > 0) {
                    map.put("tiempoDuracion", horas + "h " + minutos + "m");
                } else {
                    map.put("tiempoDuracion", minutos + "m");
                }
            } else {
                map.put("tiempoDuracion", "Sin registrar");
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }



}


