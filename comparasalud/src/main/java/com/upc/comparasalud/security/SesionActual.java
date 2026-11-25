package com.upc.comparasalud.security;

import com.upc.comparasalud.excepciones.AccesoDenegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

//Datos de quien hace la peticion. Sirve para que algunas reglas de negocio comprueben que el id que llega
//en el cuerpo (idFarmacia, idCliente) es el de la sesion, sin cambiar los endpoints.
@Component
public class SesionActual {
    public static final String DENEGADO = "No tiene permiso para esta operacion";

    private UsuarioSesion usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioSesion) return (UsuarioSesion) auth.getPrincipal();
        throw new AccesoDenegadoException(DENEGADO);
    }

    //la farmacia autenticada debe ser la indicada (si el id es null, no hay nada que comparar)
    public void exigirFarmacia(Long idFarmacia) {
        UsuarioSesion u = usuario();
        if (!"FARMACIA".equals(u.getRol()) || (idFarmacia != null && !u.getId().equals(idFarmacia)))
            throw new AccesoDenegadoException(DENEGADO);
    }

    //true si quien hace la peticion es la farmacia con ese id
    public boolean esFarmacia(Long idFarmacia) {
        UsuarioSesion u = usuario();
        return "FARMACIA".equals(u.getRol()) && u.getId().equals(idFarmacia);
    }

    public void exigirCliente(Long idCliente) {
        UsuarioSesion u = usuario();
        if (!"CLIENTE".equals(u.getRol()) || (idCliente != null && !u.getId().equals(idCliente)))
            throw new AccesoDenegadoException(DENEGADO);
    }
}
