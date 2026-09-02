package com.example.epopeyaap.model.entity;

import com.example.epopeyaap.enums.EstadoPartido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="partidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="jornada_id", nullable = false)
    private Jornada jornada;

    // partido 1, 2 o 3
    @Column(nullable = false)
    private Integer numeroPartido;

    private String lugar;

    private LocalDateTime fechaHora;

    //Pareja local (nuestro equipo)
    @ManyToOne
    @JoinColumn(name="jugador_local_1_id")
    private Usuario jugadorLocal1;

    @ManyToOne
    @JoinColumn(name="jugador_local_2")
    private Usuario jugadorLocal2;

    //Pareja visitante: el capitan introduce solo el nombre (no son usuarios del sistema)
    private String parejaVisitante1;
    private String parejaVisitante2;

    //Resultado en texto libre, ej: 6-3 6-4
    private String resultado;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPartido estado = EstadoPartido.PENDIENTE;

    // null = aun sin jugar, true = ganado, false = perdido
    private boolean ganado;
}
