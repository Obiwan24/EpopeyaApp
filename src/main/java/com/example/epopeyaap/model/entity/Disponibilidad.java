package com.example.epopeyaap.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta de un jugador a la encuesta de disponibilidad de una jorada
 * si no hay registro para un jugador, se entiende que NO está disponible
 */
@Entity
@Table(name="disponibilidades", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"jornada_id", "jugador_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disponibilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="jornada_id", nullable = false)
    private Jornada jornada;

    @ManyToOne
    @JoinColumn(name="jugador_id", nullable = false)
    private Usuario jugador;

    private boolean disponible;

    //Fecha limite hasta la que se puede responder a la encuesta
    private LocalDateTime fechaLimite;

    private LocalDateTime fechaRespuesta;

    //Una vez cerrada la encuesta, no se puede modificar
    @Builder.Default
    private boolean bloqueada = false;
}
