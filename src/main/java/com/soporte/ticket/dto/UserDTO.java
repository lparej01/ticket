package com.soporte.ticket.dto;

import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
   // private String username;
    private String email;
    private String rol; // O el tipo de dato que uses para tus roles (String, Enum, etc.)
}

