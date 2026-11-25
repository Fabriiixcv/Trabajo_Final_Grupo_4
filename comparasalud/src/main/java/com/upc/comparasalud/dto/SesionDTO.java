package com.upc.comparasalud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SesionDTO {
    private String rol;
    private Long idCliente;
    private Long idFarmacia;
    private Long idAdministrador;
    private String nombre;
    //JWT: el frontend lo envia como "Authorization: Bearer <token>"
    private String token;
}
