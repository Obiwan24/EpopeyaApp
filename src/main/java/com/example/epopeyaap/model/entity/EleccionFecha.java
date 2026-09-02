package com.example.epopeyaap.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Encuesta de eleccion de fecha cuando jugamos como visitante
 * el rival ofrece varias fechas y el equipo vota la que mas conviene
 */

@Entity
@Table(name="elecciones_fecha")
@Data
@NoArgsConstructor
@AllArgsConstructor
/*
Builder -> Lombok: permite instanciar objetos de forma legible, flexible, fluida y sin orden exacto,
de esta forma introducimos solo los datos del constructor necesarios, los que no se metan se marcan
como null y no es necesario recordar el orden.
Codigo mas limpio y legible
Siempre acompañar de @NoArgsConstructor y @AllArgsConstructor en la entidad JPA, si no, Lombok
anulara el constructor vacio por defeco y provocará un error en Hibernate al recuperar datos de la BBDD.
 */
@Builder

public class EleccionFecha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="jornada_id", nullable = false)
    private Jornada jornada;

    @Column(nullable = false)
    private String contrincante;

    @Builder.Default
    private boolean cerrada = false;

    @OneToMany(mappedBy = "eleccionFecha", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionFecha> opciones = new ArrayList<>();
}
