package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoDTO {
    //medicamentos-favoritos usa idMedicamento; farmacias-favoritas usa idFarmacia
    private Long idMedicamento;
    private Long idFarmacia;
}
