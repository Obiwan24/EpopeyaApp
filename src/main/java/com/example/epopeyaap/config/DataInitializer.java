package com.example.epopeyaap.config;

import com.example.epopeyaap.enums.Rol;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer
 * ---------------
 * NUEVO. Problema de "huevo y gallina": crear un jugador (POST /api/usuarios)
 * ahora requiere estar autenticado como CAPITAN (@PreAuthorize), pero si la
 * tabla "usuarios" está vacía, NADIE puede loguearse todavía para crear al
 * primer capitán.
 *
 * Esta clase implementa CommandLineRunner: Spring Boot ejecuta su método
 * run() automáticamente una vez, justo después de arrancar la aplicación.
 * Aquí se comprueba si la tabla de usuarios está vacía y, solo en ese caso,
 * se crea un capitán de pruebas con una contraseña conocida.
 *
 * Es seguro dejar esta clase en el proyecto: en cuanto exista al menos un
 * usuario en la base de datos (el propio capitán de pruebas, o cualquier
 * otro que crees a mano), este código no vuelve a hacer nada en los
 * siguientes arranques.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Credenciales del capitán de pruebas. Cámbialas aquí si quieres otras,
    // o simplemente cambia la contraseña desde la pantalla de Perfil una
    // vez que hayas iniciado sesión (cambioPasswordPendiente lo pedirá solo).
    private static final String USERNAME_PRUEBA = "capitan";
    private static final String PASSWORD_PRUEBA = "capitan1234";

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // ya hay usuarios: no tocamos nada
        }

        Usuario capitan = Usuario.builder()
                .username(USERNAME_PRUEBA)
                .password(passwordEncoder.encode(PASSWORD_PRUEBA))
                .nombre("Capitán")
                .apellidos("De Pruebas")
                .dni("00000000A")
                .rol(Rol.CAPITAN)
                .cambioPasswordPendiente(true) // le obligará a cambiarla en el primer login
                .build();

        usuarioRepository.save(capitan);

        log.info("=========================================================");
        log.info(" Usuario de pruebas creado (tabla 'usuarios' estaba vacía)");
        log.info(" username: {}", USERNAME_PRUEBA);
        log.info(" password: {}", PASSWORD_PRUEBA);
        log.info("=========================================================");
    }
}