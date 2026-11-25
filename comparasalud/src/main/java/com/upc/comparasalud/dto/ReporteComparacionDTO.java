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
public class ReporteComparacionDTO {
    private Long idMedicamento;
    private String medicamento;
    private int totalOfertas;
    private BigDecimal precioMinimo;
    private BigDecimal precioMaximo;
    private BigDecimal precioPromedio;
    private OfertaDTO ofertaMasEconomica;
    private List<OfertaDTO> ofertas;
}
