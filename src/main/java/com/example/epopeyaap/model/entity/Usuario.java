package com.example.epopeyaap.model.entity;

import com.example.epopeyaap.enums.Posicion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String dni;
    private String email;
    private String tlfn;
    private LocalDate fechaNacimiento;
    private Posicion posicion;
}
