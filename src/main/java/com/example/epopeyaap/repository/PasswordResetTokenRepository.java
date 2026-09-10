package com.example.epopeyaap.repository;

import com.example.epopeyaap.security.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PasswordResetTokenRepository
 * -------------------------------
 * NUEVO. Usado por AuthService para crear el token al solicitar el reset
 * y para buscarlo cuando el usuario confirma su nueva contraseña.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
}