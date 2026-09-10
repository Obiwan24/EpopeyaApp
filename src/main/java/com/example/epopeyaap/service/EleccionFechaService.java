package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.EleccionFechaCreateRequest;
import com.example.epopeyaap.model.entity.EleccionFecha;
import com.example.epopeyaap.model.entity.Jornada;
import com.example.epopeyaap.model.entity.OpcionFecha;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.model.entity.VotoFecha;
import com.example.epopeyaap.repository.EleccionFechaRepository;
import com.example.epopeyaap.repository.JornadaRepository;
import com.example.epopeyaap.repository.OpcionFechaRepository;
import com.example.epopeyaap.repository.VotoFechaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * EleccionFechaService
 * -----------------------
 * NUEVO. Da servicio a elecciones.js (encuesta de fecha cuando jugamos como
 * visitantes: el rival ofrece varias fechas y el equipo vota la que más
 * conviene).
 */
@Service
@RequiredArgsConstructor
public class EleccionFechaService {

    private final EleccionFechaRepository eleccionFechaRepository;
    private final JornadaRepository jornadaRepository;
    private final OpcionFechaRepository opcionFechaRepository;
    private final VotoFechaRepository votoFechaRepository;

    /** null si no hay ninguna elección abierta ahora mismo (elecciones.js lo trata como "sin encuesta"). */
    public EleccionFecha obtenerActiva() {
        return eleccionFechaRepository.findFirstByCerradaFalseOrderByIdDesc().orElse(null);
    }

    public EleccionFecha crear(EleccionFechaCreateRequest request) {
        if (request.fechas() == null || request.fechas().isEmpty()) {
            throw new RuntimeException("Debes proponer al menos una fecha.");
        }

        // Reutiliza la jornada si ya existe con ese número; si no, crea una nueva
        // como visitante (esLocal = false), ya que esta encuesta solo tiene
        // sentido cuando jugamos fuera de casa.
        Jornada jornada = jornadaRepository.findByNumero(request.numeroJornada())
                .orElseGet(() -> jornadaRepository.save(
                        Jornada.builder()
                                .numero(request.numeroJornada())
                                .rival(request.contrincante())
                                .esLocal(false)
                                .build()
                ));

        EleccionFecha eleccion = EleccionFecha.builder()
                .jornada(jornada)
                .contrincante(request.contrincante())
                .cerrada(false)
                .build();
        eleccion = eleccionFechaRepository.save(eleccion);

        for (String fechaTexto : request.fechas()) {
            OpcionFecha opcion = OpcionFecha.builder()
                    .eleccionFecha(eleccion)
                    .fechaHora(LocalDateTime.parse(fechaTexto))
                    .build();
            opcionFechaRepository.save(opcion);
        }

        return eleccionFechaRepository.findById(eleccion.getId()).orElseThrow();
    }

    public void votar(Long opcionId, Usuario jugador) {
        opcionFechaRepository.findById(opcionId)
                .orElseThrow(() -> new RuntimeException("Opción de fecha con el id " + opcionId + " no encontrada."));

        boolean yaVotada = votoFechaRepository.findByOpcionFechaIdAndJugadorId(opcionId, jugador.getId()).isPresent();
        if (yaVotada) {
            return; // ya estaba votada: no duplicamos el voto
        }

        OpcionFecha opcion = opcionFechaRepository.getReferenceById(opcionId);
        VotoFecha voto = VotoFecha.builder().opcionFecha(opcion).jugador(jugador).build();
        votoFechaRepository.save(voto);
    }

    public void quitarVoto(Long opcionId, Usuario jugador) {
        votoFechaRepository.findByOpcionFechaIdAndJugadorId(opcionId, jugador.getId())
                .ifPresent(votoFechaRepository::delete);
    }

    /**
     * Cierra la encuesta: fija en la jornada la fecha con más votos (en caso
     * de empate, la primera propuesta) y bloquea la elección para que deje
     * de aparecer en elecciones.js.
     */
    public void cerrar(Long eleccionId) {
        EleccionFecha eleccion = eleccionFechaRepository.findById(eleccionId)
                .orElseThrow(() -> new RuntimeException("Elección con el id " + eleccionId + " no encontrada."));

        List<OpcionFecha> opciones = eleccion.getOpciones();
        if (opciones == null || opciones.isEmpty()) {
            throw new RuntimeException("Esta elección no tiene fechas propuestas.");
        }

        OpcionFecha ganadora = opciones.stream()
                .max(Comparator.comparingInt(o -> o.getVotos() == null ? 0 : o.getVotos().size()))
                .orElseThrow();

        Jornada jornada = eleccion.getJornada();
        jornada.setFechaHoraElegida(ganadora.getFechaHora());
        jornadaRepository.save(jornada);

        eleccion.setCerrada(true);
        eleccionFechaRepository.save(eleccion);
    }
}