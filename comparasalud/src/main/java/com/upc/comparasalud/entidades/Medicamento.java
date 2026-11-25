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
@Table(name = "medicamento")
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 150, nullable = false)
    private String nombre;
    @Column(length = 150)
    private String nombreComercial;
    @Column(length = 150)
    private String principioActivo;
    @Column(length = 100)
    private String concentracion;
    @Column(length = 100)
    private String presentacion;
    @Column(length = 150)
    private String laboratorio;
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @Column(columnDefinition = "TEXT")
    private String advertencias;
    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
    //farmacia que registro el medicamento: solo ella puede modificarlo (HU30)
    @ManyToOne
    @JoinColumn(name = "id_farmacia_registro")
    private Farmacia registradoPor;
}
