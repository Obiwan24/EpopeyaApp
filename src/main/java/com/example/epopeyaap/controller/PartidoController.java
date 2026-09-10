package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.ResultadoRequest;
import com.example.epopeyaap.dto.VisitantesRequest;
import com.example.epopeyaap.model.entity.Partido;
import com.example.epopeyaap.service.PartidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PartidoController
 * -------------------
 * NUEVO. Usado por jornadas.js, exclusivamente por el capitán (los 3
 * botones "Añadir pareja visitante" / "Añadir resultado" / "Marcar N.P.").
 */
@RestController
@RequestMapping("/api/partidos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CAPITAN')")
public class PartidoController {

    private final PartidoService partidoService;

    @PutMapping("/{id}/visitantes")
    public Partido actualizarVisitantes(@PathVariable Long id, @RequestBody VisitantesRequest request) {
        return partidoService.actualizarVisitantes(id, request);
    }

    @PutMapping("/{id}/resultado")
    public Partido actualizarResultado(@PathVariable Long id, @RequestBody ResultadoRequest request) {
        return partidoService.actualizarResultado(id, request);
    }

    @PutMapping("/{id}/no-presentado")
    public Partido marcarNoPresentado(@PathVariable Long id) {
        return partidoService.marcarNoPresentado(id);
    }
}