package com.example.epopeyaap.dto;

/** Cuerpo de POST /api/auth/solicitar-reset (recuperar contraseña olvidada). */
public record SolicitudResetRequest(String username, String email) {
}