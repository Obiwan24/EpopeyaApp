package com.example.epopeyaap.dto;

import com.example.epopeyaap.enums.Posicion;
import java.time.LocalDate;

/** Cuerpo de PUT /api/usuarios/me, tal y como lo envía perfil.js. */
public record PerfilUpdateRequest(
        String dni,
        String telefono,
        String email,
        LocalDate fechaNacimiento,
        Posicion posicion
) {
}
