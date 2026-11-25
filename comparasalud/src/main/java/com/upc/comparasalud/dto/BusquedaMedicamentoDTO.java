package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaMedicamentoDTO {
    private Long id;
    private String nombre;
    private String nombreComercial;
    private List<OfertaDTO> farmacias;
}
