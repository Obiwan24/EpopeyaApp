package com.example.epopeyaap.dto;

/**
 * ConvocatoriaModificarRequest
 * -------------------------------
 * NUEVO. Cuerpo de PUT /api/convocatorias/partidos/{partidoId}.
 *
 * En el convocatoria.js original, la función "confirmarSustitucion" mandaba
 * a veces "jugador1Id" y a veces "jugador2Id" según el slot, lo cual es
 * ambiguo de leer en el backend (¿la clave que falta es "no tocar" o
 * "vaciar"?). Por eso el JS corregido (ver más abajo en mi respuesta) manda
 * siempre estos 2 campos explícitos:
 *   - slot: 1 o 2 (qué convocado se está modificando)
 *   - jugadorId: el nuevo id, o null para "desconvocar" a ese jugador
 */
public record ConvocatoriaModificarRequest(Integer slot, Long jugadorId) {
}
