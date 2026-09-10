package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.JornadaCreateRequest;
import com.example.epopeyaap.dto.PartidoCreateRequest;
import com.example.epopeyaap.enums.EstadoJornada;
import com.example.epopeyaap.enums.EstadoPartido;
import com.example.epopeyaap.model.entity.*;
import com.example.epopeyaap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JornadaService
 * --------------
 * NUEVO. No existía ningún servicio ni controlador para Jornada; era
 * imposible crear, listar o borrar jornadas, lo que a su vez bloqueaba
 * TODAS las pantallas que dependen de ellas (Jornadas, Convocatoria,
 * Disponibilidad, Elección de fecha).
 *
 * crearJornada() da servicio a DOS formularios distintos del frontend:
 *  - convocatoria.js: manda numero+equipoLocal+rival+partidos (jornada
 *    "completa": ya crea los 3 partidos y asigna convocados de golpe).
 *  - disponibilidad.js: manda solo numero+rival+fechaLimiteEncuesta (aún
 *    sin partidos: solo para lanzar la encuesta de disponibilidad).
 */
@Service
@RequiredArgsConstructor
public class JornadaService {

    private final JornadaRepository jornadaRepository;
    private final PartidoRepository partidoRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final EleccionFechaRepository eleccionFechaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Jornada> listarJornadas() {
        return jornadaRepository.findAllByOrderByNumeroDesc();
    }

    public Jornada obtenerJornada(Long id) {
        return jornadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jornada con el id " + id + " no encontrada."));
    }

    public Jornada crearJornada(JornadaCreateRequest request) {
        if (request.numero() == null || !StringUtils.hasText(request.rival())) {
            throw new RuntimeException("El número de jornada y el rival son obligatorios.");
        }

        Jornada jornada = Jornada.builder()
                .numero(request.numero())
                .rival(request.rival())
                .equipoLocal(StringUtils.hasText(request.equipoLocal()) ? request.equipoLocal() : "EPOPEYA")
                .estado(EstadoJornada.PENDIENTE)
                .build();

        if (StringUtils.hasText(request.fechaLimiteEncuesta())) {
            jornada.setFechaLimiteEncuesta(LocalDateTime.parse(request.fechaLimiteEncuesta()));
        }

        jornada = jornadaRepository.save(jornada);

        // Si el formulario ya trae los 3 partidos (pantalla Convocatoria), los creamos
        // junto con su Convocatoria si venían jugadores ya seleccionados.
        if (request.partidos() != null) {
            for (PartidoCreateRequest pReq : request.partidos()) {
                crearPartidoConConvocatoria(jornada, pReq);
            }
        }

        return jornada;
    }

    private void crearPartidoConConvocatoria(Jornada jornada, PartidoCreateRequest pReq) {
        Partido partido = Partido.builder()
                .jornada(jornada)
                .numeroPartido(pReq.numeroPartido())
                .lugar(pReq.lugar())
                .estado(EstadoPartido.PENDIENTE)
                .build();

        if (StringUtils.hasText(pReq.fechaHora())) {
            partido.setFechaHora(LocalDateTime.parse(pReq.fechaHora()));
        }
        partido = partidoRepository.save(partido);

        // Si el capitán ya eligió convocados al crear la jornada, se crea la Convocatoria
        // (todavía sin confirmar por los jugadores: eso lo hacen ellos desde su checkbox).
        if (pReq.jugador1Id() != null || pReq.jugador2Id() != null) {
            Convocatoria convocatoria = Convocatoria.builder()
                    .partido(partido)
                    .jugador1(pReq.jugador1Id() != null ? usuarioRepository.findById(pReq.jugador1Id()).orElse(null) : null)
                    .jugador2(pReq.jugador2Id() != null ? usuarioRepository.findById(pReq.jugador2Id()).orElse(null) : null)
                    .build();
            convocatoriaRepository.save(convocatoria);
        }
    }

    /**
     * Recalcula el estado de la jornada a partir del estado de sus partidos.
     * La llama PartidoService cada vez que se actualiza un partido, para que
     * el frontend (que solo lee jornada.estado, no mira partido por partido
     * en el listado) siempre esté al día.
     */
    public void recalcularEstado(Long jornadaId) {
        Jornada jornada = obtenerJornada(jornadaId);
        List<Partido> partidos = partidoRepository.findByJornadaIdOrderByNumeroPartidoAsc(jornadaId);

        if (partidos.isEmpty()) {
            return; // sin partidos todavía (jornada recién creada solo para la encuesta)
        }

        boolean todosAcabados = partidos.stream().allMatch(p ->
                p.getEstado() == EstadoPartido.FINALIZADO || p.getEstado() == EstadoPartido.NO_PRESENTADO);
        boolean algunoEnJuego = partidos.stream().anyMatch(p -> p.getEstado() == EstadoPartido.EN_JUEGO);
        boolean algunoAplazado = partidos.stream().anyMatch(p -> p.getEstado() == EstadoPartido.APLAZADO);

        if (todosAcabados) {
            jornada.setEstado(EstadoJornada.FINALIZADA);
        } else if (algunoEnJuego) {
            jornada.setEstado(EstadoJornada.EN_JUEGO);
        } else if (algunoAplazado) {
            jornada.setEstado(EstadoJornada.APLAZADA);
        } else {
            jornada.setEstado(EstadoJornada.PENDIENTE);
        }
        jornadaRepository.save(jornada);
    }

    /**
     * Elimina la jornada y TODO lo que cuelga de ella. Hay que borrar primero
     * las tablas "hijas sin cascada" (convocatorias de sus partidos y
     * elecciones de fecha) para no chocar con las restricciones de clave
     * ajena; los partidos y disponibilidades sí tienen cascada configurada.
     */
    public void eliminarJornada(Long id) {
        Jornada jornada = obtenerJornada(id);

        List<Partido> partidos = partidoRepository.findByJornadaIdOrderByNumeroPartidoAsc(id);
        for (Partido partido : partidos) {
            convocatoriaRepository.findByPartidoId(partido.getId()).ifPresent(convocatoriaRepository::delete);
        }

        disponibilidadRepository.deleteAll(disponibilidadRepository.findByJornadaId(id));

        eleccionFechaRepository.findFirstByCerradaFalseOrderByIdDesc()
                .filter(e -> e.getJornada().getId().equals(id))
                .ifPresent(eleccionFechaRepository::delete);

        jornadaRepository.delete(jornada); // cascade ALL + orphanRemoval borra los partidos restantes
    }
}