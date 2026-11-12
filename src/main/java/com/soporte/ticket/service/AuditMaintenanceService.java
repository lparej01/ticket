package com.soporte.ticket.service;

import com.soporte.ticket.repository.AuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(AuditMaintenanceService.class);

    @Autowired
    private AuditoriaRepository auditRepository;

    // Se ejecuta de forma automática el primer día de cada mes a la medianoche (00:00:00)
    @Scheduled(cron = "0 0 0 1 * ?")
    public void limpiarAuditoriaVieja() {
        log.info("Iniciando proceso automático de limpieza de auditoría...");

        // Calculamos la fecha límite (3 meses hacia atrás desde hoy)
        LocalDateTime fechaLimite = LocalDateTime.now().minusMonths(3);

        try {
            auditRepository.borrarRegistrosAntiguos(fechaLimite);
            log.info("Limpieza completada con éxito. Se eliminaron registros anteriores a: {}", fechaLimite);
        } catch (Exception e) {
            log.error("Error al ejecutar la limpieza automática de auditoría: ", e);
        }
    }
}
