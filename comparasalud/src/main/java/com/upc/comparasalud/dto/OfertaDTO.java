package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OfertaDTO {
    private Long id;
    private Long idMedicamento;
    private String nombreMedicamento;
    //farmacia que registra la oferta (HU42). Cuando se active la seguridad se tomara de la sesion
    private Long idFarmacia;
    private String nombreFarmacia;
    private String distrito;
    private BigDecimal precio;
    private Integer stock;
    private LocalDateTime fechaActualizacion;
}
