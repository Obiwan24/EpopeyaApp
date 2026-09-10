package com.example.epopeyaap.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VotoFecha
 * ---------
 * CAMBIO: @JsonIgnore en "opcionFecha" (relación inversa de
 * OpcionFecha.votos) para evitar el bucle OpcionFecha -> votos ->
 * VotoFecha.opcionFecha -> votos -> ...
 * El campo se llama "jugador" (y no "usuario", aunque apunte a la entidad
 * Usuario) porque elecciones.js ya estaba escrito esperando "v.jugador.id"
 * para saber si el usuario actual ya votó esta opción; así no hay que tocar
 * el frontend en este punto.
 */
@Entity
@Table(name="votos_fecha", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"opcion_fecha_id", "jugador_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotoFecha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="opcion_fecha_id", nullable = false)
    private OpcionFecha opcionFecha;

    @ManyToOne
    @JoinColumn(name="jugador_id", nullable = false)
    private Usuario jugador;
}