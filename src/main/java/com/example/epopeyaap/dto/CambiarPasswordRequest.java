package com.example.epopeyaap.dto;

/** Cuerpo de PUT /api/usuarios/me/password, tal y como lo envía perfil.js. */
public record CambiarPasswordRequest(String passwordActual, String passwordNueva) {
}
