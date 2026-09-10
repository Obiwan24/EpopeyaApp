package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.LoginRequest;
import com.example.epopeyaap.dto.LoginResponse;
import com.example.epopeyaap.dto.ResetPasswordRequest;
import com.example.epopeyaap.dto.SolicitudResetRequest;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.UsuarioRepository;
import com.example.epopeyaap.security.JwtUtil;
import com.example.epopeyaap.security.PasswordResetToken;
import com.example.epopeyaap.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuthService
 * -----------
 * NUEVO. Antes no existía NINGÚN endpoint de login: auth.js llamaba a
 * POST /api/auth/login y siempre recibía un 404. Esta clase es la lógica
 * detrás de AuthController.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final MailService mailService;

    /**
     * Valida username+password contra la base de datos (delegando en
     * CustomUserDetailsService + PasswordEncoder a través del
     * AuthenticationManager) y, si son correctos, genera el JWT.
     */
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            // Mensaje genérico a propósito: no decimos si falló el usuario o la
            // contraseña, para no dar pistas a quien intente adivinar cuentas.
            throw new RuntimeException("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos."));

        String token = jwtUtil.generarToken(usuario.getUsername(), usuario.getRol().name(), usuario.getId());
        return new LoginResponse(token, usuario);
    }

    /**
     * Genera un token de recuperación si el username+email coinciden con un
     * usuario real, y le envía el email. Si no coinciden, no hace nada, pero
     * IGUALMENTE responde con éxito (ver MailService) para no filtrar qué
     * cuentas existen.
     */
    public void solicitarReset(SolicitudResetRequest request) {
        usuarioRepository.findByUsername(request.username())
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(request.email()))
                .ifPresent(usuario -> {
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .token(UUID.randomUUID().toString())
                            .usuario(usuario)
                            .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                            .build();
                    resetTokenRepository.save(resetToken);
                    mailService.enviarEmailResetPassword(usuario.getEmail(), usuario.getNombre(), resetToken.getToken());
                });
    }

    /** Aplica la nueva contraseña si el token es válido y no ha caducado ni se ha usado ya. */
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("El enlace de recuperación no es válido."));

        if (resetToken.isInvalido()) {
            throw new RuntimeException("El enlace de recuperación ha caducado. Solicita uno nuevo.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuario.setCambioPasswordPendiente(false);
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        resetTokenRepository.save(resetToken);
    }
}