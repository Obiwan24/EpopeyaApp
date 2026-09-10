package com.example.epopeyaap.dto;

/**
 * LoginRequest
 * ------------
 * NUEVO. Cuerpo que espera POST /api/auth/login. Coincide exactamente con
 * lo que auth.js ya envía: { username, password }.
 * Es un "record" de Java: una forma compacta de crear una clase inmutable
 * solo para transportar datos (sin necesidad de Lombok aquí).
 */
public record LoginRequest(String username, String password) {
}