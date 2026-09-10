package com.example.epopeyaap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CorsConfig
 * ----------
 * ANTES: clase vacía. Como el HTML/JS se sirve desde el propio Spring Boot
 * (src/main/resources/static), en producción normalmente no haría falta CORS.
 * Pero durante el desarrollo (ej. abrir jugadores.html con Live Server en el
 * puerto 5500 mientras el backend corre en 8080) el navegador bloquea las
 * llamadas fetch() por política de mismo origen si no se configura CORS.
 *
 * Aquí se define explícitamente qué orígenes pueden llamar a la API. Se lee
 * de application.properties (app.cors.allowed-origins) para no tener que tocar
 * código si cambia el dominio de despliegue (ej. Render, Railway, etc.).
 */
@Configuration
public class CorsConfig {

    // Lista de orígenes permitidos separados por coma. Por defecto solo "*"
    // en desarrollo; en producción se recomienda poner el dominio real.
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuracion = new CorsConfiguration();

        if ("*".equals(allowedOrigins.trim())) {
            configuracion.addAllowedOriginPattern("*");
        } else {
            configuracion.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        }

        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuracion);
        return new CorsFilter(source);
    }
}