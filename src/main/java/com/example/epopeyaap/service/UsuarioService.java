package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.CambiarPasswordRequest;
import com.example.epopeyaap.dto.PerfilUpdateRequest;
import com.example.epopeyaap.dto.UsuarioCreateRequest;
import com.example.epopeyaap.dto.UsuarioUpdateRequest;
import com.example.epopeyaap.enums.Rol;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.UsuarioRepository;
import com.example.epopeyaap.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UsuarioService
 * --------------
 * CAMBIOS respecto al original:
 *
 * 1) crearUsuario()/modificarUsuario() ahora reciben los DTOs
 *    UsuarioCreateRequest/UsuarioUpdateRequest en vez de la entidad Usuario
 *    completa (ver el porqué en esos dos archivos: ambigüedad de Jackson +
 *    riesgo de seguridad al aceptar "rol" desde el cliente).
 *
 * 2) crearUsuario() CIFRA la contraseña con BCrypt antes de guardarla
 *    (antes se guardaba tal cual la mandaba jugadores.js: el DNI en texto
 *    plano). Sin este cambio, aunque implementemos el login con
 *    PasswordEncoder.matches(...), NUNCA coincidiría porque en BBDD no habría
 *    ningún hash con el que comparar.
 *
 * 3) Se añaden obtenerUsuarioActual/actualizarPerfil/cambiarPassword, usados
 *    por los nuevos endpoints /api/usuarios/me* que perfil.js ya necesitaba
 *    y que antes no existían.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario crearUsuario(UsuarioCreateRequest datos) {
        if (usuarioRepository.findByUsername(datos.username()).isPresent()) {
            throw new RuntimeException("Ya existe un jugador con el nombre de usuario '" + datos.username() + "'.");
        }

        Usuario usuario = Usuario.builder()
                .username(datos.username())
                // jugadores.js manda el DNI como contraseña inicial: aquí se cifra.
                .password(passwordEncoder.encode(datos.password()))
                .nombre(datos.nombre())
                .apellidos(datos.apellidos())
                .dni(datos.dni())
                .email(datos.email())
                .telefono(datos.telefono())
                .fechaNacimiento(datos.fechaNacimiento())
                .posicion(datos.posicion())
                // El rol NUNCA viene del cliente: todo alta nueva es JUGADOR.
                // Si algún día necesitas dar de alta a otro capitán, se hace
                // cambiando el rol manualmente en base de datos.
                .rol(Rol.JUGADOR)
                // Todo jugador nuevo debe cambiar su contraseña por defecto en el primer login.
                .cambioPasswordPendiente(true)
                .build();

        return usuarioRepository.save(usuario);
    }

    public Usuario modificarUsuario(Long id, UsuarioUpdateRequest datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario con el id " + id + " no encontrado."));
        usuario.setNombre(datos.nombre());
        usuario.setApellidos(datos.apellidos());
        usuario.setDni(datos.dni());
        usuario.setEmail(datos.email());
        usuario.setTelefono(datos.telefono());
        usuario.setPosicion(datos.posicion());
        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    // ----------------------------------------------------------------------
    // NUEVO: "mi perfil" (perfil.js)
    // ----------------------------------------------------------------------

    /**
     * Obtiene el Usuario autenticado a partir del contexto de seguridad que
     * rellena JwtAuthFilter en cada petición. Se usa en todos los endpoints
     * "/me" para saber "quién soy yo" sin que el frontend tenga que mandar
     * su propio id (así nadie puede editar el perfil de otro cambiando un
     * número en la URL).
     */
    public Usuario obtenerUsuarioActual() {
        UsuarioPrincipal principal = (UsuarioPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        // Se relee de BBDD (en vez de devolver directamente principal.getUsuario())
        // para tener siempre los datos más recientes, no los de cuando se generó el token.
        return usuarioRepository.findById(principal.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    public Usuario actualizarPerfil(PerfilUpdateRequest datos) {
        Usuario usuario = obtenerUsuarioActual();
        usuario.setDni(datos.dni());
        usuario.setTelefono(datos.telefono());
        usuario.setEmail(datos.email());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setPosicion(datos.posicion());
        return usuarioRepository.save(usuario);
    }

    public void cambiarPassword(CambiarPasswordRequest datos) {
        Usuario usuario = obtenerUsuarioActual();
        if (!passwordEncoder.matches(datos.passwordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual no es correcta.");
        }
        usuario.setPassword(passwordEncoder.encode(datos.passwordNueva()));
        usuario.setCambioPasswordPendiente(false);
        usuarioRepository.save(usuario);
    }
}