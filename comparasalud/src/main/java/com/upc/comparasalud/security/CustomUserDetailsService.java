package com.upc.comparasalud.security;

import com.upc.comparasalud.entidades.Administrador;
import com.upc.comparasalud.entidades.Cliente;
import com.upc.comparasalud.entidades.Farmacia;
import com.upc.comparasalud.repositorios.AdministradorRepositorio;
import com.upc.comparasalud.repositorios.ClienteRepositorio;
import com.upc.comparasalud.repositorios.FarmaciaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//Vuelve a cargar al usuario desde la base de datos en cada peticion con token, asi:
//- un cliente eliminado deja de tener acceso aunque su token no haya vencido;
//- una farmacia dada de baja (INACTIVA) tambien (HU53).
//Como hay tres tablas, el "username" que recibe es "ROL:id" (por ejemplo "CLIENTE:5").
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;
    @Autowired
    private AdministradorRepositorio administradorRepositorio;

    @Override
    public UserDetails loadUserByUsername(String clave) throws UsernameNotFoundException {
        String[] partes = clave == null ? new String[0] : clave.split(":");
        if (partes.length != 2) throw new UsernameNotFoundException("Usuario no encontrado");
        long id;
        try {
            id = Long.parseLong(partes[1]);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        return cargar(partes[0], id);
    }

    public UsuarioSesion cargar(String rol, Long id) {
        if (rol == null || id == null) throw new UsernameNotFoundException("Usuario no encontrado");
        switch (rol) {
            case "CLIENTE": {
                Cliente c = clienteRepositorio.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
                return new UsuarioSesion(rol, c.getId(), c.getCorreo());
            }
            case "FARMACIA": {
                Farmacia f = farmaciaRepositorio.findById(id)
                        .filter(x -> "ACTIVA".equals(x.getEstado()))
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
                return new UsuarioSesion(rol, f.getId(), f.getCorreo());
            }
            case "ADMINISTRADOR": {
                Administrador a = administradorRepositorio.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
                return new UsuarioSesion(rol, a.getId(), a.getCorreo());
            }
            default:
                throw new UsernameNotFoundException("Usuario no encontrado");
        }
    }
}
