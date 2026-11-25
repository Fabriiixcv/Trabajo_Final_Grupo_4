package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoPrecioDTO {
    private Long id;
    private String nombre;
    private String nombreComercial;
    private String categoria;
    private BigDecimal precioDesde;
}
