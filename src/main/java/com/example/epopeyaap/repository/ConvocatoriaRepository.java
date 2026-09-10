package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ConvocatoriaRepository
 * ------------------------
 * NUEVO. Cada Partido tiene como mucho 1 Convocatoria asociada (ver
 * @OneToOne unique=true en Convocatoria.partido). Este método es la base
 * de "buscar o crear" que usa ConvocatoriaService cuando el capitán guarda
 * los convocados de un partido por primera vez.
 */
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {

    Optional<Convocatoria> findByPartidoId(Long partidoId);
}