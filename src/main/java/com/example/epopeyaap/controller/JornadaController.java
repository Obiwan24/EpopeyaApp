package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.JornadaCreateRequest;
import com.example.epopeyaap.model.entity.Jornada;
import com.example.epopeyaap.service.JornadaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JornadaController
 * ------------------
 * NUEVO. No existía. Usado por jornadas.js, convocatoria.js y
 * disponibilidad.js:
 *   GET    /api/jornadas        -> listado (todas las pantallas)
 *   GET    /api/jornadas/{id}   -> detalle con partidos (jornadas.js)
 *   POST   /api/jornadas        -> crear (convocatoria.js y disponibilidad.js)
 *   DELETE /api/jornadas/{id}   -> eliminar (disponibilidad.js, botón "Eliminar")
 *
 * Crear y eliminar quedan restringidos al capitán con @PreAuthorize: es la
 * misma comprobación de rol que ya hacía el JS al ocultar el botón, pero
 * ahora también en el servidor.
 */
@RestController
@RequestMapping("/api/jornadas")
@RequiredArgsConstructor
public class JornadaController {

    private final JornadaService jornadaService;

    @GetMapping
    public List<Jornada> listar() {
        return jornadaService.listarJornadas();
    }

    @GetMapping("/{id}")
    public Jornada obtener(@PathVariable Long id) {
        return jornadaService.obtenerJornada(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('CAPITAN')")
    public Jornada crear(@RequestBody JornadaCreateRequest request) {
        return jornadaService.crearJornada(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CAPITAN')")
    public void eliminar(@PathVariable Long id) {
        jornadaService.eliminarJornada(id);
    }
}