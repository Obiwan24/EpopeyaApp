package com.example.epopeyaap.config;

import com.example.epopeyaap.security.CustomUserDetailsService;
import com.example.epopeyaap.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 * --------------
 * ANTES: clase vacía ("public class SecurityConfig {}"), sin @Configuration
 * ni ningún Bean. Esto es la causa raíz de que el login/seguridad de toda la
 * app no funcionase: Spring Security (que SÍ está en el pom.xml) se aplicaba
 * con su configuración por defecto — que bloquea TODO con un formulario de
 * login HTML propio de Spring, incompatible con vuestro index.html + JWT.
 *
 * Esta clase define:
 *  1) Qué rutas son públicas (login, recursos estáticos) y cuáles requieren
 *     estar autenticado (todo lo demás bajo /api/**).
 *  2) Que la sesión es STATELESS (no usamos cookies de sesión; cada petición
 *     se autentica con el JWT que manda api.js en la cabecera Authorization).
 *  3) Que el JwtAuthFilter se ejecuta antes que el filtro estándar de Spring.
 *  4) @EnableMethodSecurity para poder usar @PreAuthorize("hasRole('CAPITAN')")
 *     en los controladores y así reforzar en el SERVIDOR las restricciones
 *     que hoy solo existían en el JS (ocultar un botón no es seguridad real:
 *     cualquiera podía llamar a la API a mano con Postman).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize / @PostAuthorize en los controladores/servicios
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API sin estado -> no necesitamos protección CSRF basada en cookies
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Páginas, JS, CSS e imágenes: públicas (si no, ni la pantalla de login cargaría)
                        .requestMatchers(
                                "/", "/index.html", "/pages/**", "/js/**", "/css/**", "/assets/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Login y recuperación de contraseña: públicos por definición
                        .requestMatchers("/api/auth/**").permitAll()
                        // Cualquier otra llamada a /api/** requiere JWT válido
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )

                // Nuestro filtro JWT se ejecuta antes que el filtro de usuario/contraseña
                // por defecto de Spring Security (que aquí no usamos, pero hay que declarar el orden).
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager explícito: lo usa AuthService para validar
     * username+password en el login (delegando en CustomUserDetailsService
     * para buscar el usuario y en PasswordEncoder para comparar el hash).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}