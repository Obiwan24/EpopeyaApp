package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * DisponibilidadRepository
 * --------------------------
 * NUEVO. Sostiene la pantalla "disponibilidad.html": cada fila es la
 * respuesta (sí/no) de UN jugador a la encuesta de UNA jornada.
 */
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByJornadaId(Long jornadaId);

    Optional<Disponibilidad> findByJornadaIdAndJugadorId(Long jornadaId, Long jugadorId);
}