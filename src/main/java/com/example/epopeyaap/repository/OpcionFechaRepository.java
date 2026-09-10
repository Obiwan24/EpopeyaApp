package com.example.epopeyaap.repository;

import com.example.epopeyaap.model.entity.OpcionFecha;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * OpcionFechaRepository
 * -----------------------
 * NUEVO. CRUD estándar de JpaRepository es suficiente: OpcionFechaService
 * siempre busca por id (findById) para votar/desvotar una fecha concreta.
 */
public interface OpcionFechaRepository extends JpaRepository<OpcionFecha, Long> {
}
