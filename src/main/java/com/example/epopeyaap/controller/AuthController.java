package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.LoginRequest;
import com.example.epopeyaap.dto.LoginResponse;
import com.example.epopeyaap.dto.ResetPasswordRequest;
import com.example.epopeyaap.dto.SolicitudResetRequest;
import com.example.epopeyaap.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 * --------------
 * NUEVO. Antes no existía NINGÚN controlador de autenticación, así que
 * auth.js (POST /api/auth/login) siempre recibía un 404. Estas rutas están
 * en SecurityConfig como "permitAll()" porque, obviamente, hay que poder
 * llamarlas SIN estar todavía autenticado.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/solicitar-reset")
    public void solicitarReset(@RequestBody SolicitudResetRequest request) {
        authService.solicitarReset(request);
    }

    /** Aún sin pantalla HTML que lo consuma, pero listo para cuando la montes. */
    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }
}
