package com.soporte.ticket.model.enums;

public enum EstadoTicket {
    ABIERTO,     // El ticket fue creado pero ningún técnico lo ha revisado
    EN_PROCESO,  // Un técnico ya fue asignado y está trabajando en la solución
    RESUELTO,    // El técnico marcó el problema como solucionado
    CERRADO      // El cliente o administrador confirmó que la solución es conforme
}
