package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.ConvocatoriaModificarRequest;
import com.example.epopeyaap.dto.PartidoConvocatoriaResponse;
import com.example.epopeyaap.model.entity.Convocatoria;
import com.example.epopeyaap.security.UsuarioPrincipal;
import com.example.epopeyaap.service.ConvocatoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ConvocatoriaController
 * -------------------------
 * NUEVO. Da servicio a convocatoria.js:
 *   GET /api/convocatorias/jornadas/{jornadaId}      -> partidos + convocados de una jornada
 *   PUT /api/convocatorias/partidos/{partidoId}      -> el capitán asigna/cambia un convocado
 *   PUT /api/convocatorias/{id}/confirmar?slot=1|2   -> el propio jugador confirma su convocatoria
 *
 * "modificar" queda restringido al capitán; "confirmar" lo puede llamar
 * cualquier usuario autenticado, pero el servicio comprueba que sea
 * realmente el jugador de ese slot (ver ConvocatoriaService.confirmar).
 */
@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
public class ConvocatoriaController {

    private final ConvocatoriaService convocatoriaService;

    @GetMapping("/jornadas/{jornadaId}")
    public List<PartidoConvocatoriaResponse> listarPorJornada(@PathVariable Long jornadaId) {
        return convocatoriaService.listarPorJornada(jornadaId);
    }

    @PutMapping("/partidos/{partidoId}")
    @PreAuthorize("hasRole('CAPITAN')")
    public Convocatoria modificarConvocado(@PathVariable Long partidoId, @RequestBody ConvocatoriaModificarRequest request) {
        return convocatoriaService.modificarConvocado(partidoId, request);
    }

    @PutMapping("/{id}/confirmar")
    public Convocatoria confirmar(
            @PathVariable Long id,
            @RequestParam int slot,
            @AuthenticationPrincipal UsuarioPrincipal principal
    ) {
        return convocatoriaService.confirmar(id, slot, principal.getUsuario());
    }
}