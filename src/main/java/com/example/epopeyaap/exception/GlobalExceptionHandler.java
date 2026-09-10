package com.example.epopeyaap.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * GlobalExceptionHandler
 * -----------------------
 * NUEVO. Antes, cualquier error en un servicio (ej. "usuario no encontrado")
 * se traducía en un error 500 con un cuerpo HTML genérico de Spring Boot
 * ("Whitelabel Error Page"), que api.js no sabía interpretar:
 *   const mensaje = (data && data.mensaje) ? data.mensaje : "Error inesperado...";
 *
 * Con esta clase, cualquier RuntimeException lanzada desde un Service (con
 * throw new RuntimeException("mensaje para el usuario")) se convierte
 * automáticamente en JSON { "mensaje": "..." } con código 400, que es
 * exactamente lo que el frontend ya sabe mostrar en los alert()/mensajes de error.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> manejarAccesoDenegado(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensaje", "No tienes permiso para realizar esta acción."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorInesperado(Exception e) {
        log.error("Error inesperado no controlado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error inesperado en el servidor."));
    }
}