package com.example.epopeyaap.model.entity;

import com.example.epopeyaap.enums.Posicion;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
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

    //Getters y Setters

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getApellido1() {return apellido1;}
    public void setApellido1(String apellido1) {this.apellido1 = apellido1;}

    public String getApellido2() {return apellido2;}
    public void setApellido2(String apellido2) {this.apellido2 = apellido2;}

    public String getDni() {return dni;}
    public void setDni(String dni) {this.dni = dni;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getTlfn() {return tlfn;}
    public void setTlfn(String tlfn) {this.tlfn = tlfn;}

    public Posicion getPosicion() {return posicion;}
    public void setPosicion(Posicion posicion) {this.posicion = posicion;}
}
