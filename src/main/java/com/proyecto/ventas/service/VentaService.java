package com.proyecto.ventas.service;

import com.proyecto.ventas.dto.ClienteDTO;
import com.proyecto.ventas.dto.ProductoDTO;
import com.proyecto.ventas.dto.UsuarioDTO;
import com.proyecto.ventas.dto.VentaRequestDTO;
import com.proyecto.ventas.model.DetalleVenta;
import com.proyecto.ventas.model.Venta;
import com.proyecto.ventas.repository.VentaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers; // <-- ASEGÚRATE DE TENER ESTE IMPORT

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired @Qualifier("webClientCliente")
    private WebClient webClientCliente;

    @Autowired @Qualifier("webClientProducto")
    private WebClient webClientProducto;

    @Autowired @Qualifier("webClientInventario")
    private WebClient webClientInventario;

    @Autowired @Qualifier("webClientUsuario")
    private WebClient webClientUsuario;
    
    // =================================================================
    // MÉTODOS NUEVOS PARA CONSULTAS (LO QUE TE FALTABA AÑADIR)
    // =================================================================

    public Flux<Venta> obtenerTodasLasVentasReactivo() {
        return Flux.defer(() -> Flux.fromIterable(ventaRepository.findAll()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Venta> obtenerVentaPorIdReactiva(Long id) {
        return Mono.fromCallable(() -> ventaRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalVenta -> optionalVenta.map(Mono::just).orElse(Mono.empty()));
    }

    // =================================================================
    // TU CÓDIGO PARA CREAR VENTAS (QUE YA ESTÁ PERFECTO)
    // =================================================================

    @Transactional
    public Mono<Venta> crearVentaReactiva(VentaRequestDTO ventaRequest) {
        Mono<UsuarioDTO> vendedorMono = validarUsuario(ventaRequest.getVendedorId());
        Mono<ClienteDTO> clienteMono = validarCliente(ventaRequest.getClienteId());
        Mono<Map<Long, ProductoDTO>> productosMapMono = obtenerMapaDeProductos(ventaRequest);

        return Mono.zip(vendedorMono, clienteMono, productosMapMono)
            .flatMap(tupla -> {
                Map<Long, ProductoDTO> productosMap = tupla.getT3();

                Venta nuevaVenta = new Venta();
                nuevaVenta.setClienteId(ventaRequest.getClienteId());
                nuevaVenta.setVendedor_Id(ventaRequest.getVendedorId());
                nuevaVenta.setFecha(LocalDate.now());

                List<DetalleVenta> detalles = ventaRequest.getDetalles().stream()
                    .map(detalleDto -> {
                        ProductoDTO producto = productosMap.get(detalleDto.getProductoId());
                        if (producto == null) {
                            throw new RuntimeException("Producto no encontrado: " + detalleDto.getProductoId());
                        }
                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setProductoId(detalleDto.getProductoId());
                        detalle.setCantidad(detalleDto.getCantidad());
                        detalle.setPrecioUnitario(producto.getPrecioVenta());
                        return detalle;
                    }).collect(Collectors.toList());
                
                double totalVenta = detalles.stream()
                    .mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad())
                    .sum();
                
                nuevaVenta.setDetalles(detalles);
                nuevaVenta.setTotal(totalVenta);

                return actualizarStock(ventaRequest)
                    .then(Mono.fromCallable(() -> ventaRepository.save(nuevaVenta)));
            });
    }

    private Mono<UsuarioDTO> validarUsuario(Long vendedorId) {
        return webClientUsuario.get().uri("/{id}", vendedorId).retrieve()
            .bodyToMono(UsuarioDTO.class)
            .switchIfEmpty(Mono.error(new RuntimeException("Vendedor no encontrado: " + vendedorId)));
    }

    private Mono<ClienteDTO> validarCliente(Long clienteId) {
        return webClientCliente.get().uri("/{id}", clienteId).retrieve()
            .bodyToMono(ClienteDTO.class)
            .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado: " + clienteId)));
    }

    private Mono<Map<Long, ProductoDTO>> obtenerMapaDeProductos(VentaRequestDTO request) {
        List<Long> ids = request.getDetalles().stream()
            .map(d -> d.getProductoId())
            .collect(Collectors.toList());
        
        return webClientProducto.get()
            .uri(uriBuilder -> uriBuilder.path("/batch").queryParam("ids", ids).build())
            .retrieve()
            .bodyToFlux(ProductoDTO.class)
            .collectMap(ProductoDTO::getId, Function.identity());
    }

   private Mono<Void> actualizarStock(VentaRequestDTO request) {
        return Flux.fromIterable(request.getDetalles())
            .flatMap(detalle -> 
                webClientInventario.post()
                    .uri("/{productoId}/salida?cantidad={cantidad}", detalle.getProductoId(), detalle.getCantidad())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(e -> {
                        System.err.println("Error al actualizar stock para producto " + detalle.getProductoId() + ": " + e.getMessage());
                        return Mono.error(new RuntimeException("No se pudo actualizar el stock para el producto " + detalle.getProductoId()));
                    })
            )
            .then();
    }
}