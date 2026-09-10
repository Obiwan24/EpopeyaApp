package com.example.epopeyaap.dto;

/**
 * PartidoCreateRequest
 * ----------------------
 * NUEVO. Representa uno de los 3 partidos que vienen dentro del formulario
 * "Añadir Jornada" de convocatoria.html. "fechaHora" llega como texto
 * "yyyy-MM-ddTHH:mm" (así lo construye convocatoria.js concatenando el
 * <input type="date"> y el <input type="time">), por eso aquí se recibe como
 * String y es JornadaService quien lo convierte a LocalDateTime (controlando
 * el caso de que venga vacío, para no reventar con una excepción de parseo).
 */
public record PartidoCreateRequest(
        Integer numeroPartido,
        String fechaHora,
        String lugar,
        Long jugador1Id,
        Long jugador2Id
) {
}