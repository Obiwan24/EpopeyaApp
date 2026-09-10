package com.example.epopeyaap.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter
 * -------------
 * ANTES: clase vacía. Este filtro es la pieza que, en CADA petición HTTP que
 * llega a /api/**, lee la cabecera "Authorization: Bearer <token>", valida el
 * token y le dice a Spring Security "esta petición viene autenticada como
 * este usuario, con este rol". Sin este filtro, aunque JwtUtil generase
 * tokens perfectos, el backend nunca sabría quién hace cada petición y
 * SecurityConfig no podría aplicar reglas como "solo CAPITAN puede borrar".
 *
 * Se registra en SecurityConfig ANTES del filtro estándar de usuario/contraseña
 * de Spring Security, para que se ejecute en cada request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String cabeceraAuth = request.getHeader("Authorization");

        // Si no hay cabecera "Bearer ...", dejamos pasar la petición sin autenticar
        // (SecurityConfig decidirá si esa ruta requiere estar logueado o no).
        if (cabeceraAuth == null || !cabeceraAuth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = cabeceraAuth.substring(7); // quita "Bearer "

        try {
            final String username = jwtUtil.extraerUsername(token);

            // Solo autenticamos si aún no hay nadie autenticado en este contexto
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails usuario = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validarToken(token, usuario.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token corrupto, usuario ya no existe, etc. -> seguimos sin autenticar;
            // la petición acabará devolviendo 401/403 si la ruta lo requiere.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
