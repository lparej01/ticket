package com.soporte.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <-- ACTIVA EL SOPORTE PARA TAREAS PROGRAMADAS
@EnableCaching // <-- ACTIVA EL ALMACENAMIENTO EN CACHÉ
public class TicketApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketApplication.class, args);
	}

}
