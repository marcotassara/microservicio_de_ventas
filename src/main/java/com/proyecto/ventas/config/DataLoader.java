package com.proyecto.ventas.config;

import com.proyecto.ventas.dto.ClienteDTO;
import com.proyecto.ventas.dto.DetalleVentaDTO;
import com.proyecto.ventas.dto.ProductoDTO;
import com.proyecto.ventas.dto.UsuarioDTO;
import com.proyecto.ventas.dto.VentaRequestDTO;
import com.proyecto.ventas.repository.VentaRepository;
import com.proyecto.ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private VentaService ventaService;

    @Autowired @Qualifier("webClientCliente")
    private WebClient webClientCliente;

    @Autowired @Qualifier("webClientProducto")
    private WebClient webClientProducto;

    @Autowired @Qualifier("webClientUsuario")
    private WebClient webClientUsuario;

    @Override
    public void run(String... args) {
        if (ventaRepository.count() == 0) {
            System.out.println("🧪 No hay ventas registradas, creando datos de prueba...");

            try {
                // Obtenemos los datos necesarios de otros servicios (aquí .block() está bien para el arranque)
                ClienteDTO[] clientes = webClientCliente.get().uri("").retrieve().bodyToMono(ClienteDTO[].class).block();
                ProductoDTO[] productos = webClientProducto.get().uri("").retrieve().bodyToMono(ProductoDTO[].class).block();
                UsuarioDTO[] vendedores = webClientUsuario.get().uri("").retrieve().bodyToMono(UsuarioDTO[].class).block();

                if (clientes == null || clientes.length == 0 || productos == null || productos.length < 3 || vendedores == null || vendedores.length == 0) {
                    System.out.println("⚠️ No hay suficientes clientes, productos o vendedores para crear ventas de prueba.");
                    return;
                }

                Long vendedorIdDePrueba = vendedores[0].getVendedor_id();
                System.out.println("👤 Usando al vendedor '" + vendedores[0].getNombreCompleto() + "' para las ventas.");

                // --- Simulamos la primera venta ---
                System.out.println("-> Simulando Venta 1...");
                VentaRequestDTO venta1 = new VentaRequestDTO();
                venta1.setClienteId(clientes[0].getId());
                venta1.setVendedorId(vendedorIdDePrueba);
                venta1.setDetalles(Arrays.asList(
                    crearDetalle(productos[0].getId(), 2),
                    crearDetalle(productos[1].getId(), 1)
                ));
                
                // CAMBIO CLAVE: Llamamos al nuevo método reactivo y lo bloqueamos.
                // Esto asegura que la venta se complete antes de continuar.
                ventaService.crearVentaReactiva(venta1).block();
                System.out.println("   Venta para " + clientes[0].getNombre() + " registrada.");

                // --- Simulamos la segunda venta ---
                System.out.println("-> Simulando Venta 2...");
                VentaRequestDTO venta2 = new VentaRequestDTO();
                venta2.setClienteId(clientes[1].getId());
                venta2.setVendedorId(vendedorIdDePrueba);
                venta2.setDetalles(List.of(
                    crearDetalle(productos[2].getId(), 5)
                ));

                // CAMBIO CLAVE: Hacemos lo mismo para la segunda venta.
                ventaService.crearVentaReactiva(venta2).block();
                System.out.println("   Venta para " + clientes[1].getNombre() + " registrada.");

                System.out.println("✅ " + ventaRepository.count() + " ventas de prueba creadas exitosamente.");

            } catch (Exception e) {
                System.err.println("❌ Error al crear ventas de prueba: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ La tabla de ventas ya tiene datos.");
        }
    }

    private DetalleVentaDTO crearDetalle(Long productoId, Integer cantidad) {
        DetalleVentaDTO detalle = new DetalleVentaDTO();
        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        return detalle;
    }
}