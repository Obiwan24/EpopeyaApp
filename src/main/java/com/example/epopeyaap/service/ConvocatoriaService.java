package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.ConvocatoriaModificarRequest;
import com.example.epopeyaap.dto.PartidoConvocatoriaResponse;
import com.example.epopeyaap.model.entity.Convocatoria;
import com.example.epopeyaap.model.entity.Partido;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.ConvocatoriaRepository;
import com.example.epopeyaap.repository.PartidoRepository;
import com.example.epopeyaap.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ConvocatoriaService
 * -------------------
 * NUEVO. Es la pieza central de todo lo que pediste para la pantalla
 * Convocatoria:
 *  - listarPorJornada(): los 3 partidos de una jornada + quién está
 *    convocado en cada uno (o null si aún no se ha asignado a nadie).
 *  - modificarConvocado(): el capitán asigna/cambia/quita un convocado de
 *    un partido. Cada vez que se cambia, se resetea su confirmación (tiene
 *    que volver a confirmar) y se retira de la pantalla de Jornadas hasta
 *    que confirme de nuevo (ver más abajo por qué).
 *  - confirmar(): el propio jugador marca su casilla de "voy convocado".
 *    EN ESTE MOMENTO, y no antes, es cuando el jugador "aparece
 *    automáticamente en la pantalla de jornadas en el partido
 *    correspondiente": aquí es donde se copia el convocado desde
 *    Convocatoria.jugador1/2 hacia Partido.jugadorLocal1/2, que es el campo
 *    que lee jornadas.js.
 */
@Service
@RequiredArgsConstructor
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<PartidoConvocatoriaResponse> listarPorJornada(Long jornadaId) {
        List<Partido> partidos = partidoRepository.findByJornadaIdOrderByNumeroPartidoAsc(jornadaId);
        return partidos.stream()
                .map(p -> new PartidoConvocatoriaResponse(
                        p.getId(),
                        p.getNumeroPartido(),
                        p.getFechaHora(),
                        p.getLugar(),
                        convocatoriaRepository.findByPartidoId(p.getId()).orElse(null)
                ))
                .toList();
    }

    public Convocatoria modificarConvocado(Long partidoId, ConvocatoriaModificarRequest request) {
        if (request.slot() == null || (request.slot() != 1 && request.slot() != 2)) {
            throw new RuntimeException("El slot del convocado debe ser 1 o 2.");
        }

        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido con el id " + partidoId + " no encontrado."));

        Convocatoria convocatoria = convocatoriaRepository.findByPartidoId(partidoId)
                .orElseGet(() -> Convocatoria.builder().partido(partido).build());

        Usuario nuevoJugador = null;
        if (request.jugadorId() != null) {
            nuevoJugador = usuarioRepository.findById(request.jugadorId())
                    .orElseThrow(() -> new RuntimeException("Jugador con el id " + request.jugadorId() + " no encontrado."));
        }

        if (request.slot() == 1) {
            convocatoria.setJugador1(nuevoJugador);
            convocatoria.setConfirmadoJugador1(false);
            partido.setJugadorLocal1(null); // hasta que vuelva a confirmar, desaparece de "Jornadas"
        } else {
            convocatoria.setJugador2(nuevoJugador);
            convocatoria.setConfirmadoJugador2(false);
            partido.setJugadorLocal2(null);
        }

        partidoRepository.save(partido);
        convocatoria = convocatoriaRepository.save(convocatoria);

        if (nuevoJugador != null) {
            nuevoJugador.getEstadistica().setVecesConvocado(nuevoJugador.getEstadistica().getVecesConvocado() + 1);
            usuarioRepository.save(nuevoJugador);
        }

        return convocatoria;
    }

    /**
     * Confirmación individual de un jugador convocado. Se comprueba que el
     * usuario autenticado sea realmente el jugador de ese slot (para que
     * nadie pueda confirmar la convocatoria de otro compañero).
     */
    public Convocatoria confirmar(Long convocatoriaId, int slot, Usuario usuarioAutenticado) {
        Convocatoria convocatoria = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new RuntimeException("Convocatoria con el id " + convocatoriaId + " no encontrada."));

        Partido partido = convocatoria.getPartido();

        if (slot == 1) {
            comprobarEsElJugador(convocatoria.getJugador1(), usuarioAutenticado);
            convocatoria.setConfirmadoJugador1(true);
            partido.setJugadorLocal1(convocatoria.getJugador1());
        } else if (slot == 2) {
            comprobarEsElJugador(convocatoria.getJugador2(), usuarioAutenticado);
            convocatoria.setConfirmadoJugador2(true);
            partido.setJugadorLocal2(convocatoria.getJugador2());
        } else {
            throw new RuntimeException("El slot de confirmación debe ser 1 o 2.");
        }

        partidoRepository.save(partido);
        return convocatoriaRepository.save(convocatoria);
    }

    private void comprobarEsElJugador(Usuario jugadorDelSlot, Usuario usuarioAutenticado) {
        if (jugadorDelSlot == null || !jugadorDelSlot.getId().equals(usuarioAutenticado.getId())) {
            throw new RuntimeException("Solo puedes confirmar tu propia convocatoria.");
        }
    }
}
