package com.example.epopeyaap.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="opciones_fecha")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionFecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="eleccion_fecha_id", nullable = false)
    private EleccionFecha eleccionFecha;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @OneToMany(mappedBy = "opcionFecha", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VotoFecha> votos = new ArrayList<>();
}
