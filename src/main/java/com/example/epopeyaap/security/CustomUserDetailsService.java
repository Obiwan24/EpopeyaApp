package com.example.epopeyaap.security;

import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService
 * -------------------------
 * ANTES: clase vacía. Sin esto, Spring Security no tiene forma de saber
 * "dado un username, qué contraseña (hash) y qué rol tiene ese usuario",
 * así que cualquier intento de autenticación fallaba en seco.
 *
 * Esta clase implementa la interfaz estándar UserDetailsService: recibe un
 * username y devuelve un UserDetails (en nuestro caso, un UsuarioPrincipal
 * que envuelve la entidad Usuario). Se usa en dos sitios:
 *  1) AuthService, al validar el login manualmente.
 *  2) JwtAuthFilter, para reconstruir el usuario autenticado a partir del
 *     username que viene dentro del token JWT en cada petición.
 */
@Service
@RequiredArgsConstructor // Lombok: genera un constructor con los campos "final" (usuarioRepository)
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe ningún usuario con el nombre de usuario '" + username + "'"));
        return new UsuarioPrincipal(usuario);
    }
}