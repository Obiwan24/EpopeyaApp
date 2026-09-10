package com.example.epopeyaap.dto;

import com.example.epopeyaap.model.entity.Usuario;

import java.time.LocalDateTime;

/**
 * Una fila de la tabla de disponibilidad: un jugador y su respuesta (o
 * "null" en disponible si todavía no ha contestado). Coincide con lo que
 * disponibilidad.js ya esperaba: r.jugador, r.disponible, r.fechaRespuesta, r.bloqueada.
 */
public record RespuestaDisponibilidadResponse(
        Usuario jugador,
        Boolean disponible, // null = todavía no ha respondido
        LocalDateTime fechaRespuesta,
        boolean bloqueada
) {
}