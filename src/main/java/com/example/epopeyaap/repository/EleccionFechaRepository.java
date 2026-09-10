package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.EleccionFecha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * EleccionFechaRepository
 * -------------------------
 * NUEVO. Sostiene "elecciones.html": la encuesta de fecha cuando jugamos
 * como visitantes. Solo puede haber una elección "activa" (no cerrada) a la
 * vez, que es justo lo que pide elecciones.js en GET /elecciones-fecha/activa.
 */
public interface EleccionFechaRepository extends JpaRepository<EleccionFecha, Long> {

    Optional<EleccionFecha> findFirstByCerradaFalseOrderByIdDesc();
}