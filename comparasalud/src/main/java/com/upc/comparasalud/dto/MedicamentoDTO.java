package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoDTO {
    private Long id;
    private String nombre;
    private String nombreComercial;
    private String principioActivo;
    private String concentracion;
    private String presentacion;
    private String laboratorio;
    private String descripcion;
    private String advertencias;
    private Long idCategoria;
    private String nombreCategoria;
    //farmacia que registra (HU22) o que modifica (HU30). Cuando se active la seguridad se tomara de la sesion
    private Long idFarmacia;
}
