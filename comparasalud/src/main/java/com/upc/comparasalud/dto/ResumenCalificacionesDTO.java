package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCalificacionesDTO {
    private int total;
    private BigDecimal promedio;
    private List<CalificacionDetalleDTO> calificaciones;
}
