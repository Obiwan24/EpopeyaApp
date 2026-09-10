package com.example.epopeyaap.model.entity;

import com.example.epopeyaap.enums.EstadoJornada;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="jornadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @Builder.Default
    private String equipoLocal = "EPOPEYA";

    @Column(nullable = false)
    private String rival;

    private LocalDate fechaInicioRango;
    private LocalDate fechaFinRango;

    //Fecha y hora elegida para jugar la jornada
    private LocalDateTime fechaHoraElegida;

    // Limite para responder a encuesta de disponibilidad
    private LocalDateTime fechaLimiteEncuesta;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoJornada estado = EstadoJornada.PENDIENTE;

    //true si jugamos en casa, false visitante
    @Builder.Default
    private boolean esLocal = true;

    @OneToMany(mappedBy = "jornada", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Partido> partidos = new ArrayList<>();
}
