package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * NUEVO: imprescindible para el login (CustomUserDetailsService) y para
     * comprobar duplicados al crear un jugador nuevo. Spring Data JPA genera
     * la consulta automáticamente a partir del nombre del método.
     */
    Optional<Usuario> findByUsername(String username);
}
