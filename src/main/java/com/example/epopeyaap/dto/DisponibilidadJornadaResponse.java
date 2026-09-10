package com.example.epopeyaap.dto;

import com.example.epopeyaap.model.entity.Jornada;

import java.util.List;

/**
 * Respuesta de GET /api/disponibilidad/jornadas/{id}. Coincide con el
 * comentario que ya había en disponibilidad.js:
 *   // datos = { jornada, respuestas: [{jugador, disponible, fechaRespuesta, bloqueada}] }
 */
public record DisponibilidadJornadaResponse(Jornada jornada, List<RespuestaDisponibilidadResponse> respuestas) {
}