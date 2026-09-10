package com.example.epopeyaap.controller;

import com.example.epopeyaap.dto.CambiarPasswordRequest;
import com.example.epopeyaap.dto.PerfilUpdateRequest;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UsuarioController
 * ------------------
 * CAMBIOS respecto al original:
 *
 * 1) @PreAuthorize("hasRole('CAPITAN')") en crear/modificar/eliminar: antes
 *    cualquier usuario con sesión iniciada podía dar de alta o borrar
 *    jugadores llamando a la API directamente (el JS solo ocultaba el botón,
 *    lo cual NO es seguridad real). Ahora el propio backend lo impide.
 *
 * 2) Se añaden los 3 endpoints "/me*" que perfil.js ya necesitaba:
 *    GET /api/usuarios/me, PUT /api/usuarios/me y PUT /api/usuarios/me/password.
 *    Cualquier usuario autenticado puede leer/editar SU PROPIO perfil (no
 *    hace falta ser capitán).
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioService.obtenerTodosUsuarios();
    }

    @PostMapping
    @PreAuthorize("hasRole('CAPITAN')")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioService.crearUsuario(usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CAPITAN')")
    public Usuario modificarUsuario(@PathVariable Long id, @RequestBody Usuario nuevosDatos) {
        return usuarioService.modificarUsuario(id, nuevosDatos);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CAPITAN')")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }

    // ------------------------------------------------------------------
    // NUEVO: "mi perfil" (perfil.js)
    // ------------------------------------------------------------------

    @GetMapping("/me")
    public Usuario obtenerMiPerfil() {
        return usuarioService.obtenerUsuarioActual();
    }

    @PutMapping("/me")
    public Usuario actualizarMiPerfil(@RequestBody PerfilUpdateRequest datos) {
        return usuarioService.actualizarPerfil(datos);
    }

    @PutMapping("/me/password")
    public void cambiarMiPassword(@RequestBody CambiarPasswordRequest datos) {
        usuarioService.cambiarPassword(datos);
    }
}