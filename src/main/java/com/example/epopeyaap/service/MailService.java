package com.example.epopeyaap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * MailService
 * -----------
 * NUEVO. Envuelve el JavaMailSender (ver MailConfig) para que el resto de la
 * app no tenga que lidiar con excepciones de SMTP directamente.
 *
 * Importante: si el envío falla (credenciales no configuradas, sin acceso a
 * internet en el contenedor, etc.) NO se relanza la excepción hacia arriba.
 * Motivo: por seguridad, "solicitar-reset" siempre debe responder con éxito
 * exista o no ese username/email (así nadie puede usar ese endpoint para
 * averiguar qué usuarios existen). Si el correo no llega, queda registrado
 * en el log del servidor para que puedas revisarlo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void enviarEmailResetPassword(String destinatario, String nombre, String token) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("EpopeyApp - Recuperación de contraseña");
            mensaje.setText(
                    "Hola " + nombre + ",\n\n" +
                            "Hemos recibido una solicitud para restablecer tu contraseña.\n" +
                            "Este es tu código de recuperación (válido 30 minutos):\n\n" +
                            token + "\n\n" +
                            "Si no has sido tú, puedes ignorar este mensaje."
            );
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de recuperación a {}: {}", destinatario, e.getMessage());
        }
    }
}