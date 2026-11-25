package com.upc.comparasalud.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "oferta_medicamento",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_medicamento", "id_farmacia"}))
public class OfertaMedicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_farmacia", nullable = false)
    private Farmacia farmacia;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;
    @Column(nullable = false)
    private Integer stock;
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}
