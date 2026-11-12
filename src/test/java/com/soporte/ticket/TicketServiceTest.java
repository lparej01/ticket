package com.soporte.ticket;



import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.soporte.ticket.model.Ticket;
import com.soporte.ticket.repository.TicketRepository;
import com.soporte.ticket.service.impl.TicketServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticketAbierto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticketAbierto = new Ticket();
        ticketAbierto.setId(1L);
        ticketAbierto.setEstado("ABIERTO");
        ticketAbierto.setTecnico(null);
    }

    @Test
    void listarDisponibles_DebeRetornarSoloTicketsAbiertosSinTecnico() {
        // Arrange: Configuramos el mock para que responda con nuestro ticket de prueba
        when(ticketRepository.findByEstadoAndTecnicoIsNull("ABIERTO"))
                .thenReturn(List.of(ticketAbierto));

        List<Ticket> resultado = ticketService.obtenerTicketsDisponibles();

        System.out.println("\n==================================================");
        System.out.println("🧪 ¡EJECUTANDO PRUEBA UNITARIA DE TICKETS DISPONIBLES!");
        System.out.println("Cantidad de tickets recibidos: " + resultado.size());
        if (!resultado.isEmpty()) {
            System.out.println("ID del Ticket: " + resultado.get(0).getId());
            System.out.println("Estado en BD:  " + resultado.get(0).getEstado());
            System.out.println("Técnico Asig:  " + resultado.get(0).getTecnico());
        }
        System.out.println("==================================================\n");


        // Assert: Validaciones de negocio
        assertNotNull(resultado, "La lista no debe ser null");
        assertEquals(1, resultado.size(), "Debe contener exactamente 1 ticket");
        assertEquals("ABIERTO", resultado.get(0).getEstado());
        assertNull(resultado.get(0).getTecnico(), "El técnico asignado debe ser null");


        verify(ticketRepository, times(1)).findByEstadoAndTecnicoIsNull("ABIERTO");
    }
}


