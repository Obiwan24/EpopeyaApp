package com.example.epopeyaap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoderConfig
 * ---------------------
 * ANTES: esta clase estaba vacía (solo "package ... class PasswordEncoderConfig {}"),
 * por lo que Spring nunca tuvo un PasswordEncoder disponible y NADIE en la app
 * cifraba contraseñas: se guardaban en texto plano en la base de datos y el login
 * no tenía forma de compararlas de forma segura.
 *
 * Esta clase expone un único Bean de tipo PasswordEncoder (BCrypt) que se inyecta
 * en:
 *  - UsuarioService, para cifrar la contraseña al crear un jugador o al resetearla.
 *  - AuthService, para comparar la contraseña introducida en el login con el hash
 *    guardado en BBDD (encoder.matches(passwordPlano, hashGuardado)).
 *
 * BCrypt es el estándar recomendado por Spring Security: genera un "salt" distinto
 * en cada hash, así que dos usuarios con la misma contraseña NUNCA tienen el mismo
 * valor guardado en la tabla usuarios.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}