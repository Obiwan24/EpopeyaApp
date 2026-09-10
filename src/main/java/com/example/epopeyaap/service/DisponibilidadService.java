package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.DisponibilidadJornadaResponse;
import com.example.epopeyaap.dto.RespuestaDisponibilidadResponse;
import com.example.epopeyaap.enums.Rol;
import com.example.epopeyaap.model.entity.Disponibilidad;
import com.example.epopeyaap.model.entity.Jornada;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.DisponibilidadRepository;
import com.example.epopeyaap.repository.JornadaRepository;
import com.example.epopeyaap.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DisponibilidadService
 * ----------------------
 * NUEVO. Da servicio a disponibilidad.js:
 *  - listarJornadasConEncuesta(): jornadas con fechaLimiteEncuesta rellena.
 *  - obtenerDetalle(): la jornada + la respuesta (o ausencia de respuesta)
 *    de CADA jugador del equipo, para que el capitán vea quién falta por
 *    contestar.
 *  - responder(): un jugador (o el capitán en su nombre) marca sí/no. Si no
 *    existía fila para ese jugador+jornada, se crea; si ya existía, se
 *    actualiza (siempre que no esté "bloqueada").
 */
@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final JornadaRepository jornadaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Jornada> listarJornadasConEncuesta() {
        return jornadaRepository.findByFechaLimiteEncuestaIsNotNullOrderByNumeroDesc();
    }

    public DisponibilidadJornadaResponse obtenerDetalle(Long jornadaId) {
        Jornada jornada = jornadaRepository.findById(jornadaId)
                .orElseThrow(() -> new RuntimeException("Jornada con el id " + jornadaId + " no encontrada."));

        List<Usuario> todosLosJugadores = usuarioRepository.findAll();

        List<RespuestaDisponibilidadResponse> respuestas = todosLosJugadores.stream()
                .map(jugador -> disponibilidadRepository.findByJornadaIdAndJugadorId(jornadaId, jugador.getId())
                        .map(d -> new RespuestaDisponibilidadResponse(jugador, d.isDisponible(), d.getFechaRespuesta(), d.isBloqueada()))
                        // Si el jugador todavía no ha respondido: se muestra sin marcar (ni sí ni no) y editable.
                        .orElseGet(() -> new RespuestaDisponibilidadResponse(jugador, null, null, false)))
                .toList();

        return new DisponibilidadJornadaResponse(jornada, respuestas);
    }

    public Disponibilidad responder(Long jornadaId, Long jugadorId, boolean disponible, Usuario usuarioAutenticado) {
        boolean esElMismoJugador = usuarioAutenticado.getId().equals(jugadorId);
        boolean esCapitan = usuarioAutenticado.getRol() == Rol.CAPITAN;
        if (!esElMismoJugador && !esCapitan) {
            throw new RuntimeException("Solo puedes responder tu propia disponibilidad.");
        }

        Jornada jornada = jornadaRepository.findById(jornadaId)
                .orElseThrow(() -> new RuntimeException("Jornada con el id " + jornadaId + " no encontrada."));
        Usuario jugador = usuarioRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador con el id " + jugadorId + " no encontrado."));

        Disponibilidad disponibilidad = disponibilidadRepository.findByJornadaIdAndJugadorId(jornadaId, jugadorId)
                .orElse(Disponibilidad.builder()
                        .jornada(jornada)
                        .jugador(jugador)
                        .fechaLimite(jornada.getFechaLimiteEncuesta())
                        .build());

        if (disponibilidad.isBloqueada()) {
            throw new RuntimeException("La encuesta de esta jornada ya está cerrada.");
        }

        boolean esRespuestaNueva = disponibilidad.getId() == null;
        disponibilidad.setDisponible(disponible);
        disponibilidad.setFechaRespuesta(LocalDateTime.now());
        disponibilidad = disponibilidadRepository.save(disponibilidad);

        // Solo se cuenta una vez en las estadísticas, la primera vez que este
        // jugador contesta "disponible" a esta jornada (no en cada cambio de opinión).
        if (esRespuestaNueva && disponible) {
            jugador.getEstadistica().setVecesDisponible(jugador.getEstadistica().getVecesDisponible() + 1);
            usuarioRepository.save(jugador);
        }

        return disponibilidad;
    }
}