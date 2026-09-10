package com.example.epopeyaap.dto;

import java.util.List;

/**
 * JornadaCreateRequest
 * -----------------------
 * NUEVO. Cuerpo de POST /api/jornadas. Cubre los DOS formularios distintos
 * que ya existían en el frontend y que apuntaban a este mismo endpoint:
 *
 *  - convocatoria.js -> manda numero, equipoLocal, rival y los 3 "partidos"
 *    (con convocados incluidos): crea la jornada completa de golpe.
 *  - disponibilidad.js -> manda solo numero, rival y fechaLimiteEncuesta
 *    (crea la jornada para lanzar la encuesta de disponibilidad, sin
 *    partidos todavía; se añadirán después desde Convocatoria).
 *
 * Todos los campos menos "numero" y "rival" son opcionales (pueden llegar
 * a null), así que JornadaService.crearJornada() comprueba cada uno antes
 * de usarlo.
 */
public record JornadaCreateRequest(
        Integer numero,
        String rival,
        String equipoLocal,
        String fechaLimiteEncuesta, // "yyyy-MM-ddTHH:mm", opcional
        List<PartidoCreateRequest> partidos // opcional
) {
}
