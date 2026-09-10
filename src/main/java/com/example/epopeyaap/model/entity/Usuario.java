package com.example.epopeyaap.model.entity;

import com.example.epopeyaap.enums.Posicion;
import com.example.epopeyaap.enums.Rol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore // no se serializa hacia el frontend (seguridad)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    //Se guarda cifrado en Jasypt (ver DniEncryptor)
    @Column(unique = true, nullable = false)
    private String dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    @Enumerated(EnumType.STRING)
    private Posicion posicion;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Builder.Default
    private boolean cambioPasswordPendiente = false;

    // Estadisticas del jugador (usadas por /api/estadisticas y estadisticas.js)
    @Embedded
    @Builder.Default
    private EstadisticaJugador estadistica = new EstadisticaJugador();
}
