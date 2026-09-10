package com.example.epopeyaap.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Convocatoria de un partido concreto (2 jugadores convocados + confirmacion individual)
 * cada partido tiene un máximo de 1 convocatoria asociada
 *
 * CAMBIO: @JsonIgnore en "partido". La Convocatoria siempre viaja anidada
 * DENTRO de la respuesta del partido (ver dto/PartidoConvocatoriaResponse),
 * así que serializar aquí otra vez el partido completo sería redundante
 * (y innecesariamente pesado).
 */
@Entity
@Table(name="convocatorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convocatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name="partido_id", nullable = false, unique = true)
    private Partido partido;

    @ManyToOne
    @JoinColumn(name="jugador_1_id")
    private Usuario jugador1;

    @ManyToOne
    @JoinColumn(name="jugador_2_id")
    private Usuario jugador2;

    @Builder.Default
    private boolean confirmadoJugador1 = false;

    @Builder.Default
    private boolean confirmadoJugador2 = false;
}