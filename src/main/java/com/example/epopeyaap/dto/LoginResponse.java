package com.example.epopeyaap.dto;

import com.example.epopeyaap.model.entity.Usuario;

/**
 * LoginResponse
 * -------------
 * NUEVO. Respuesta de POST /api/auth/login. auth.js hace:
 *   setToken(data.token);
 *   setUsuarioActual(data.usuario);
 *   if (data.usuario.cambioPasswordPendiente) { ... }
 * así que necesitamos exactamente estos dos campos. Se puede devolver la
 * entidad Usuario tal cual porque ya tiene @JsonIgnore en "password".
 */
public record LoginResponse(String token, Usuario usuario) {
}