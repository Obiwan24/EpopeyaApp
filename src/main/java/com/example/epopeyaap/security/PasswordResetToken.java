package com.example.epopeyaap.security;

import com.example.epopeyaap.model.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PasswordResetToken
 * -------------------
 * ANTES: clase vacía. Ahora es una entidad JPA real: cada vez que un jugador
 * pide "he olvidado mi contraseña" (form-recuperar en index.html -> auth.js ->
 * POST /api/auth/solicitar-reset), se genera una fila aquí con un código
 * aleatorio (UUID) y una fecha de caducidad corta (p. ej. 30 minutos).
 *
 * El email que se envía contiene un enlace con ese "token" como parámetro.
 * Cuando el usuario lo abre y pone su nueva contraseña, el backend busca esta
 * fila por token, comprueba que no haya caducado ni se haya usado ya, y
 * actualiza la contraseña del Usuario asociado.
 *
 * Nota: de momento el proyecto no tiene una pantalla HTML para consumir este
 * token (no había ningún reset-password.html en el volcado que me pasaste),
 * así que he dejado el endpoint /api/auth/reset-password listo en el backend
 * para cuando quieras montar esa pantalla.
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Builder.Default
    private boolean usado = false;

    /** true si el token ya caducó o ya fue canjeado. */
    public boolean isInvalido() {
        return usado || LocalDateTime.now().isAfter(fechaExpiracion);
    }
}