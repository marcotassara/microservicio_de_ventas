package com.proyecto.ventas.dto;

import lombok.Data;
import java.util.List;

@Data
public class VentaRequestDTO {
    private Long clienteId;
    private Long vendedorId;
    private List<DetalleVentaDTO> detalles;
}