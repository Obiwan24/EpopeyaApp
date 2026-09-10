package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.DisponibilidadJornadaResponse;
import com.example.epopeyaap.dto.DisponibilidadUpdateRequest;
import com.example.epopeyaap.model.entity.Disponibilidad;
import com.example.epopeyaap.model.entity.Jornada;
import com.example.epopeyaap.security.UsuarioPrincipal;
import com.example.epopeyaap.service.DisponibilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DisponibilidadController
 * ---------------------------
 * NUEVO. Da servicio a disponibilidad.js:
 *   GET /api/disponibilidad/jornadas                          -> jornadas con encuesta abierta
 *   GET /api/disponibilidad/jornadas/{id}                     -> jornada + respuestas de todo el equipo
 *   PUT /api/disponibilidad/jornadas/{id}/jugadores/{jugId}   -> marcar sí/no
 *
 * Nota: crear (POST /api/jornadas) y eliminar (DELETE /api/jornadas/{id})
 * jornadas con encuesta se gestionan desde JornadaController, porque
 * conceptualmente son operaciones sobre Jornada, no sobre Disponibilidad.
 */
@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping("/jornadas")
    public List<Jornada> listarJornadasConEncuesta() {
        return disponibilidadService.listarJornadasConEncuesta();
    }

    @GetMapping("/jornadas/{id}")
    public DisponibilidadJornadaResponse obtenerDetalle(@PathVariable Long id) {
        return disponibilidadService.obtenerDetalle(id);
    }

    @PutMapping("/jornadas/{jornadaId}/jugadores/{jugadorId}")
    public Disponibilidad responder(
            @PathVariable Long jornadaId,
            @PathVariable Long jugadorId,
            @RequestBody DisponibilidadUpdateRequest request,
            @AuthenticationPrincipal UsuarioPrincipal principal
    ) {
        return disponibilidadService.responder(jornadaId, jugadorId, request.disponible(), principal.getUsuario());
    }
}