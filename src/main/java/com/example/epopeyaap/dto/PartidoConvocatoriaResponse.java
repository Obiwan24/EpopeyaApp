package com.example.epopeyaap.dto;

import com.example.epopeyaap.model.entity.Convocatoria;

import java.time.LocalDateTime;

/**
 * PartidoConvocatoriaResponse
 * -------------------------------
 * NUEVO. Lo que devuelve GET /api/convocatorias/jornadas/{jornadaId}: la
 * lista de los 3 partidos de la jornada, cada uno con su Convocatoria
 * (o null si el capitán aún no ha asignado a nadie a ese partido).
 * Coincide con lo que convocatoria.js ya esperaba leer:
 *   p.numeroPartido, p.fechaHora, p.lugar, p.convocatoria.jugador1, etc.
 */
public record PartidoConvocatoriaResponse(
        Long id,
        Integer numeroPartido,
        LocalDateTime fechaHora,
        String lugar,
        Convocatoria convocatoria // puede ser null
) {
}