package com.proyecto.ventas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UsuarioDTO {
     @JsonProperty("id")
    private Long vendedor_id;
    private String email;
    private String nombreCompleto;
    private String rol;
}