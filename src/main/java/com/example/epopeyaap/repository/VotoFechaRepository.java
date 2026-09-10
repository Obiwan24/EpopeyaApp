package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.VotoFecha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * VotoFechaRepository
 * ----------------------
 * NUEVO. Permite comprobar si un jugador ya votó una opción concreta
 * (para no duplicar voto) y borrar su voto si pulsa otra vez el botón.
 */
public interface VotoFechaRepository extends JpaRepository<VotoFecha, Long> {

    Optional<VotoFecha> findByOpcionFechaIdAndJugadorId(Long opcionFechaId, Long jugadorId);
}