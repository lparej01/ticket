package com.soporte.ticket.service.impl;

import com.soporte.ticket.dto.TicketRequestDTO;
import com.soporte.ticket.dto.TicketResponseDTO;
import com.soporte.ticket.mapper.TicketMapper;
import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.TicketRepository;
import com.soporte.ticket.repository.UserRepository;
import com.soporte.ticket.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository usuarioRepository;
    private final TicketMapper mapper;

    // Inyección de dependencias por constructor (Estándar empresarial)
    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository usuarioRepository, TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TicketResponseDTO crearTicket(TicketRequestDTO dto, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new RuntimeException("Usuario cliente no encontrado"));

        Ticket ticket = new Ticket();
        ticket.setTitulo(dto.getTitulo());
        ticket.setDescripcion(dto.getDescripcion());
        ticket.setCliente(cliente);

        return mapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarTodos() {
        return ticketRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarPorCliente(Long clienteId) {
        // AQUÍ CONECTAMOS EL MÉTODO DEL REPOSITORIO CON EL GUION BAJO
        return ticketRepository.findByCliente_Id(clienteId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketResponseDTO asignarTecnico(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        ticket.setTecnico(tecnico);
        ticket.setEstado("EN_PROCESO");
        return mapper.toDto(ticketRepository.save(ticket));
    }

    @Override
    public List<TicketResponseDTO> listarPorClienteEmail(String clienteEmail) {
        List<Ticket> tickets = ticketRepository.findByClienteEmail(clienteEmail);
        TicketMapper mapper = new TicketMapper();

        return tickets.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> obtenerTicketsDisponibles() {
        return (List<Ticket>) ticketRepository.findByEstadoAndTecnicoIsNull("ABIERTO");
    }


}

