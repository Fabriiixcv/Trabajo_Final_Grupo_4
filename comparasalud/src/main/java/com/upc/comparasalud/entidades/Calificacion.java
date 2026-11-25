package com.upc.comparasalud.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calificacion")
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_farmacia", nullable = false)
    private Farmacia farmacia;
    @Column(nullable = false)
    private Integer puntuacion;
    @Column(columnDefinition = "TEXT")
    private String comentario;
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}
