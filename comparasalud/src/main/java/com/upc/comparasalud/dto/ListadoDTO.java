package com.upc.comparasalud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

//Respuesta del panel del administrador: el total y el detalle de cada registro
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ListadoDTO<T> {
    private long total;
    private List<T> detalle;
}
