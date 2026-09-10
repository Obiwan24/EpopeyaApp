package com.example.epopeyaap.service;

import com.example.epopeyaap.dto.ResultadoRequest;
import com.example.epopeyaap.dto.VisitantesRequest;
import com.example.epopeyaap.enums.EstadoPartido;
import com.example.epopeyaap.model.entity.Convocatoria;
import com.example.epopeyaap.model.entity.EstadisticaJugador;
import com.example.epopeyaap.model.entity.Partido;
import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.ConvocatoriaRepository;
import com.example.epopeyaap.repository.PartidoRepository;
import com.example.epopeyaap.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PartidoService
 * --------------
 * NUEVO. Da servicio a los 3 botones de capitán en jornadas.html/js:
 * "Añadir pareja visitante", "Añadir resultado" y "Marcar N.P.".
 * También actualiza las estadísticas (EstadisticaJugador) de los 2
 * convocados de ese partido cuando se registra un resultado.
 */
@Service
@RequiredArgsConstructor
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final JornadaService jornadaService;

    private Partido obtenerPartido(Long id) {
        return partidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partido con el id " + id + " no encontrado."));
    }

    public Partido actualizarVisitantes(Long partidoId, VisitantesRequest request) {
        Partido partido = obtenerPartido(partidoId);
        partido.setParejaVisitante1(request.parejaVisitante1());
        partido.setParejaVisitante2(request.parejaVisitante2());
        return partidoRepository.save(partido);
    }

    public Partido actualizarResultado(Long partidoId, ResultadoRequest request) {
        Partido partido = obtenerPartido(partidoId);
        partido.setResultado(request.resultado());
        partido.setGanado(request.ganado());
        partido.setEstado(EstadoPartido.FINALIZADO);
        partido = partidoRepository.save(partido);

        actualizarEstadisticasDelPartido(partido, request.ganado());
        jornadaService.recalcularEstado(partido.getJornada().getId());
        return partido;
    }

    public Partido marcarNoPresentado(Long partidoId) {
        Partido partido = obtenerPartido(partidoId);
        partido.setEstado(EstadoPartido.NO_PRESENTADO);
        // Un "No Presentado" del rival se cuenta como partido ganado para nuestro equipo.
        partido.setGanado(true);
        partido = partidoRepository.save(partido);

        actualizarEstadisticasDelPartido(partido, true);
        jornadaService.recalcularEstado(partido.getJornada().getId());
        return partido;
    }

    /**
     * Suma partido ganado/perdido y, de forma orientativa, sets y juegos
     * ganados a los 2 jugadores convocados en ese partido, interpretando el
     * texto libre del resultado (ej: "6-3 6-4") como "juegos local-juegos
     * visitante" por set. Si el capitán escribe el resultado en otro
     * formato, esta parte simplemente no suma esos números (no revienta).
     */
    private void actualizarEstadisticasDelPartido(Partido partido, boolean ganado) {
        Convocatoria convocatoria = convocatoriaRepository.findByPartidoId(partido.getId()).orElse(null);
        if (convocatoria == null) {
            return; // partido sin convocatoria asociada: no hay a quién sumar estadísticas
        }

        int[] setsYJuegos = contarSetsYJuegosGanados(partido.getResultado());

        for (Usuario jugador : List.of(convocatoria.getJugador1(), convocatoria.getJugador2())) {
            if (jugador == null) continue;
            EstadisticaJugador est = jugador.getEstadistica();
            if (ganado) {
                est.setPartidosGanados(est.getPartidosGanados() + 1);
            } else {
                est.setPartidosPerdidos(est.getPartidosPerdidos() + 1);
            }
            est.setSetGanados(est.getSetGanados() + setsYJuegos[0]);
            est.setJuegosGanados(est.getJuegosGanados() + setsYJuegos[1]);
            usuarioRepository.save(jugador);
        }
    }

    /** devuelve {setsGanados, juegosGanados} a partir de un texto tipo "6-3 6-4 7-5" */
    private int[] contarSetsYJuegosGanados(String resultado) {
        int sets = 0, juegos = 0;
        if (resultado == null || resultado.isBlank()) return new int[]{0, 0};

        for (String set : resultado.trim().split("\\s+")) {
            String[] partes = set.split("-");
            if (partes.length != 2) continue;
            try {
                int local = Integer.parseInt(partes[0].trim());
                int visitante = Integer.parseInt(partes[1].trim());
                juegos += local;
                if (local > visitante) sets++;
            } catch (NumberFormatException ignorado) {
                // el capitán no ha escrito "6-3": ignoramos ese "set" para el cálculo
            }
        }
        return new int[]{sets, juegos};
    }
}