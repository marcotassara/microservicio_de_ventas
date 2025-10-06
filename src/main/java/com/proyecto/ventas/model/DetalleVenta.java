package com.proyecto.ventas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "DETALLES_VENTA")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_venta_seq")
    @SequenceGenerator(name = "detalle_venta_seq", sequenceName = "DETALLE_VENTA_SEQ", allocationSize = 1)
    @Column(name = "IDDETALLE")
    private Long id;

    @Column(name = "IDPRODUCTO", nullable = false)
    private Long productoId;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO", nullable = false)
    private Double precioUnitario;
}