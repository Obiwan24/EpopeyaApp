package com.example.epopeyaap.controller;

import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * EstadisticaController
 * -------------------------
 * NUEVO. Da servicio a estadisticas.js: GET /api/estadisticas.
 * Reutiliza UsuarioService.obtenerTodosUsuarios() porque las estadísticas
 * viven embebidas dentro de Usuario (campo "estadistica"); no hace falta un
 * servicio ni repositorio propios.
 */
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticaController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.obtenerTodosUsuarios();
    }
}