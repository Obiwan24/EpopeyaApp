package com.example.epopeyaap.security;

import com.example.epopeyaap.model.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UsuarioPrincipal
 * ----------------
 * CLASE NUEVA (no existía).
 *
 * Spring Security trabaja internamente con la interfaz UserDetails, no con
 * nuestra entidad Usuario directamente. Esta clase es el "adaptador" entre
 * ambos mundos: envuelve un Usuario y expone lo que Spring Security necesita
 * (username, password cifrada, y los "roles"/authorities).
 *
 * Ventaja de guardar el Usuario completo dentro del principal: en cualquier
 * controlador podemos hacer
 *      @AuthenticationPrincipal UsuarioPrincipal principal
 * y acceder directamente a principal.getUsuario().getId() o .getRol() sin
 * tener que volver a consultar la base de datos.
 */
public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    /** Acceso directo a la entidad original, útil desde los controladores. */
    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security espera el prefijo "ROLE_" para que hasRole("CAPITAN")
        // funcione en @PreAuthorize. El enum Rol ya vale CAPITAN o JUGADOR.
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}