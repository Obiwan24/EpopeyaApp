package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.Jornada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JornadaRepository
 * ------------------
 * NUEVO. Antes no existía ningún repositorio para Jornada, así que
 * JornadaController/JornadaService no tenían forma de leer/guardar nada
 * en la tabla "jornadas".
 */
public interface JornadaRepository extends JpaRepository<Jornada, Long> {

    // Listado general (pantallas Jornadas y Convocatoria): más reciente primero
    List<Jornada> findAllByOrderByNumeroDesc();

    // Jornadas con encuesta de disponibilidad activa (pantalla Disponibilidad)
    List<Jornada> findByFechaLimiteEncuestaIsNotNullOrderByNumeroDesc();

    Optional<Jornada> findByNumero(Integer numero);
}
