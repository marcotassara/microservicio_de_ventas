package com.proyecto.ventas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "VENTAS")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venta_seq")
    @SequenceGenerator(name = "venta_seq", sequenceName = "VENTA_SEQ", allocationSize = 1)
    @Column(name = "IDVENTA")
    private Long id;

    @Column(name = "IDCLIENTE", nullable = false)
    private Long clienteId;

    @Column(name = "vendedor_Id", nullable = false) 
    private Long vendedor_Id;

    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @Column(name = "TOTAL", nullable = false)
    private Double total;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) 
    @JoinColumn(name = "IDVENTA")
    private List<DetalleVenta> detalles;
}