package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PartidoRepository
 * -------------------
 * NUEVO. Necesario para PartidoController/PartidoService (resultado,
 * pareja visitante, no presentado) y para ConvocatoriaService (listar los
 * 3 partidos de una jornada concreta).
 */
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    List<Partido> findByJornadaIdOrderByNumeroPartidoAsc(Long jornadaId);
}