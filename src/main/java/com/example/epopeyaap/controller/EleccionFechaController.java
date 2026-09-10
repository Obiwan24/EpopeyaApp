package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.EleccionFechaCreateRequest;
import com.example.epopeyaap.model.entity.EleccionFecha;
import com.example.epopeyaap.security.UsuarioPrincipal;
import com.example.epopeyaap.service.EleccionFechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * EleccionFechaController
 * ---------------------------
 * NUEVO. Da servicio a elecciones.js:
 *   GET    /api/elecciones-fecha/activa                 -> encuesta abierta (o null)
 *   POST   /api/elecciones-fecha                         -> crear (capitán)
 *   POST   /api/elecciones-fecha/opciones/{id}/voto      -> votar una fecha
 *   DELETE /api/elecciones-fecha/opciones/{id}/voto      -> quitar el voto
 *   PUT    /api/elecciones-fecha/{id}/cerrar              -> cerrar y fijar la fecha ganadora (capitán)
 */
@RestController
@RequestMapping("/api/elecciones-fecha")
@RequiredArgsConstructor
public class EleccionFechaController {

    private final EleccionFechaService eleccionFechaService;

    @GetMapping("/activa")
    public EleccionFecha obtenerActiva() {
        return eleccionFechaService.obtenerActiva();
    }

    @PostMapping
    @PreAuthorize("hasRole('CAPITAN')")
    public EleccionFecha crear(@RequestBody EleccionFechaCreateRequest request) {
        return eleccionFechaService.crear(request);
    }

    @PostMapping("/opciones/{opcionId}/voto")
    public void votar(@PathVariable Long opcionId, @AuthenticationPrincipal UsuarioPrincipal principal) {
        eleccionFechaService.votar(opcionId, principal.getUsuario());
    }

    @DeleteMapping("/opciones/{opcionId}/voto")
    public void quitarVoto(@PathVariable Long opcionId, @AuthenticationPrincipal UsuarioPrincipal principal) {
        eleccionFechaService.quitarVoto(opcionId, principal.getUsuario());
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('CAPITAN')")
    public void cerrar(@PathVariable Long id) {
        eleccionFechaService.cerrar(id);
    }
}