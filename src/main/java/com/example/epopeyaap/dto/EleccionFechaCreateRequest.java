package com.example.epopeyaap.dto;

import java.util.List;

/**
 * Cuerpo de POST /api/elecciones-fecha (elecciones.js).
 * "fechas" llega como lista de textos "yyyy-MM-ddTHH:mm", una por cada
 * <input> que el capitán rellenó en los prompt() sucesivos.
 */
public record EleccionFechaCreateRequest(Integer numeroJornada, String contrincante, List<String> fechas) {
}
