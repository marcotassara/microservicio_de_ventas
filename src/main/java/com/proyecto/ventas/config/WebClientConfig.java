package com.proyecto.ventas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClientCliente(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8085/api/v1/clientes").build();
    }

    @Bean
    public WebClient webClientProducto(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8082/api/v1/productos").build();
    }

    @Bean
    public WebClient webClientInventario(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8084/api/v1/inventario").build();
    }

    @Bean
    public WebClient webClientUsuario(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8083/api/v1/usuarios").build();
    }

    

}