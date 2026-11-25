package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionDTO {
    private Long id;
    //cliente que califica. Cuando se active la seguridad se tomara de la sesion
    private Long idCliente;
    private Long idFarmacia;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaCreacion;
}
