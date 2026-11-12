package com.soporte.ticket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private String estado;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;


    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaResolucion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"password", "rol", "authorities", "username", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    @JsonIgnoreProperties({"password", "rol", "authorities", "username", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario tecnico;

    @Column(nullable = false)
    private String prioridad;

    @Column(columnDefinition = "TEXT")
    private String resolucion;
}
