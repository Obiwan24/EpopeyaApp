package com.example.epopeyaap.dto;

import com.example.epopeyaap.enums.Posicion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * UsuarioUpdateRequest
 * -----------------------
 * NUEVO. Mismo motivo que UsuarioCreateRequest: PUT /api/usuarios/{id}
 * tampoco debería aceptar la entidad completa (evita el mismo riesgo de
 * ambigüedad de Jackson y de que alguien cuele un "rol" o "password" por
 * esta vía). Solo los campos que guardarCambiosJugador() de jugadores.js
 * realmente envía.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioUpdateRequest(
        String nombre,
        String apellidos,
        String dni,
        String email,
        String telefono,
        Posicion posicion
) {
}