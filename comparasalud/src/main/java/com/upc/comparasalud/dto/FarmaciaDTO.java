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
public class FarmaciaDTO {
    private Long id;
    private String nombre;
    private String licencia;
    private String correo;
    private String contrasena;
    private String direccion;
    private String distrito;
    private String telefono;
    private String horario;
    private String estado;
}
