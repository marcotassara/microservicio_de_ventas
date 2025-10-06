package com.proyecto.ventas.controller;

import com.proyecto.ventas.dto.VentaRequestDTO;
import com.proyecto.ventas.model.Venta;
import com.proyecto.ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono; // CAMBIO: Importamos Mono
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping("/{id}")
    // CAMBIO: El método ahora devuelve un Mono para ser asíncrono.
    public Mono<ResponseEntity<Venta>> getVentaById(@PathVariable Long id) {
        // CAMBIO: Llamamos al nuevo método reactivo del servicio.
        return ventaService.obtenerVentaPorIdReactiva(id)
                .map(venta -> ResponseEntity.ok(venta)) // Si se encuentra, responde 200 OK con la venta.
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Si el Mono está vacío, responde 404 Not Found.
    }

    @GetMapping
    // CAMBIO: El método devuelve un Mono que contiene la lista.
    public Mono<ResponseEntity<List<Venta>>> getAllVentas() {
        // CAMBIO: Llamamos al método reactivo que devuelve un Flux y lo convertimos a una lista.
        return ventaService.obtenerTodasLasVentasReactivo()
                .collectList() // Agrupa todas las ventas del Flux en un Mono<List<Venta>>.
                .map(ventas -> ResponseEntity.ok(ventas)); // Responde 200 OK con la lista.
    }

    @PostMapping
    // CAMBIO: El método devuelve un Mono para manejar la creación de forma asíncrona.
    public Mono<ResponseEntity<Venta>> registrarVenta(@RequestBody VentaRequestDTO ventaRequest) {
        // CAMBIO: Llamamos al método de creación reactivo que ya habías hecho.
        return ventaService.crearVentaReactiva(ventaRequest)
                .map(nuevaVenta -> ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta)); // Responde 201 Created.
    }
}