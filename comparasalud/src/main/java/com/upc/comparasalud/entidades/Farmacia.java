package com.upc.comparasalud.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "farmacia")
public class Farmacia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 150, nullable = false)
    private String nombre;
    @Column(length = 100, nullable = false, unique = true)
    private String licencia;
    @Column(length = 150, nullable = false, unique = true)
    private String correo;
    @Column(nullable = false)
    private String contrasena;
    @Column(length = 250, nullable = false)
    private String direccion;
    @Column(length = 100, nullable = false)
    private String distrito;
    @Column(length = 30, nullable = false)
    private String telefono;
    @Column(length = 200, nullable = false)
    private String horario;
    //ACTIVA | INACTIVA (baja logica)
    @Column(length = 20, nullable = false)
    private String estado;
}
