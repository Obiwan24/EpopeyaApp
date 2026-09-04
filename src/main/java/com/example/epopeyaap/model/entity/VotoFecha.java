package com.example.epopeyaap.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    @JoinColumn(name="opcion_fecha_id", nullable = false)
    private OpcionFecha opcionFecha;

    @ManyToOne
    @JoinColumn(name="jugador_id", nullable = false)
    private Usuario usuario;
}
