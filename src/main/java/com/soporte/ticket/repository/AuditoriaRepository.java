package com.soporte.ticket.repository;

import com.soporte.ticket.model.Auditoria;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    // 1. It searches the entire audit and sorts it from most recent to oldest.
    List<Auditoria> findAllByOrderByFechaDesc();

    // 2. Search for a specific user's audit, sorted by descending date.
    List<Auditoria> findByUsuarioOrderByFechaDesc(String usuario);

    @Modifying
    @Transactional
    @Query("DELETE FROM Auditoria a WHERE a.fecha < :limite")
    void borrarRegistrosAntiguos(@Param("limite") LocalDateTime limite);
}

