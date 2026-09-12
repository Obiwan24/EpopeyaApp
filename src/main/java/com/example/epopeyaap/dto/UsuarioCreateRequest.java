package com.example.epopeyaap.dto;

import com.example.epopeyaap.enums.Posicion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/**
 * UsuarioCreateRequest
 * -----------------------
 * NUEVO. Antes, POST /api/usuarios recibía directamente la entidad JPA
 * "Usuario" como @RequestBody. Eso daba dos problemas:
 *
 * 1) Ambigüedad de deserialización: Usuario tiene varios constructores
 *    generados por Lombok (@NoArgsConstructor, @AllArgsConstructor), y con
 *    ciertas combinaciones de campos ausentes en el JSON (como
 *    "cambioPasswordPendiente", que jugadores.js nunca envía porque lo
 *    decide el backend), Jackson podía intentar usar el constructor de
 *    "todos los parámetros" y fallar con:
 *      "Cannot map `null` into type `boolean`"
 *    porque no hay ningún valor que poner ahí.
 *
 * 2) Seguridad: al aceptar la entidad completa, un cliente podría (por
 *    error o a propósito) mandar "rol": "CAPITAN" al crear un jugador nuevo
 *    y auto-concederse permisos de capitán. Por eso este DTO NI SIQUIERA
 *    tiene un campo "rol": UsuarioService.crearUsuario() asigna siempre
 *    Rol.JUGADOR, sin excepciones.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true): por si el frontend todavía
 * manda alguna clave de más (como el antiguo "rol": "JUGADOR"), se ignora
 * en vez de reventar la petición.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioCreateRequest(
        String username,
        String password,
        String nombre,
        String apellidos,
        String dni,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        Posicion posicion
) {
}