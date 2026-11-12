package com.soporte.ticket.repository;

import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {


    List<Ticket> findByCliente_Id(Long clienteId);

    List<Ticket> findByEstado(String estado);

    // Buscar todos los tickets creados por un cliente específico
    List<Ticket> findByCliente(Usuario cliente);

    // Buscar todos los tickets asignados a un técnico específico
    List<Ticket> findByTecnico(Usuario tecnico);

    List<Ticket> findByTecnicoAndEstado(Usuario tecnico, String estado);

    List<Ticket> findByClienteOrderByFechaCreacionDesc(Usuario cliente);

    List<Ticket> findByClienteEmail(String email);

    List<Ticket> findByEstadoOrderByFechaCreacionDesc(String estado);

    // Trae los tickets que están en estado ABIERTO y donde el objeto técnico sea NULL (sin asignar)
    List<Ticket> findByEstadoAndTecnicoIsNullOrderByFechaCreacionDesc(String estado);


    // CONSULTA DIRECTA A LA BASE DE DATOS: Ignora cualquier filtro de Spring Security o Hibernate
    @Query("SELECT t FROM Ticket t ORDER BY t.id DESC")
    List<Ticket> findSubremeAllTicketsNativo();


    Object findByEstadoAndTecnicoIsNull(String abierto);
}


