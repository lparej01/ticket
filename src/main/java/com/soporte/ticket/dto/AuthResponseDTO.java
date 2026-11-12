package com.soporte.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private String rol;

    public AuthResponseDTO(String token,String rol) {
        this.token = token;
        this.rol = rol;

    }
}

