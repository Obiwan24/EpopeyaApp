package com.example.epopeyaap.dto;

/**
 * Cuerpo de POST /api/auth/reset-password. No hay todavía una pantalla HTML
 * que lo use (no había reset-password.html en el proyecto), pero queda listo
 * en el backend para cuando la crees: el enlace del email llevaría el
 * "token" como parámetro de la URL.
 */
public record ResetPasswordRequest(String token, String nuevaPassword) {
}