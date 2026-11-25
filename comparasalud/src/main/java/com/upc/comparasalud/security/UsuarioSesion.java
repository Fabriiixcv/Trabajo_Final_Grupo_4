package com.upc.comparasalud.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

//Quien esta autenticado: rol (CLIENTE | FARMACIA | ADMINISTRADOR), su id en su tabla y su correo.
//Los tres roles viven en tablas distintas, por eso el id y el rol viajan dentro del token.
public class UsuarioSesion implements UserDetails {
    private final String rol;
    private final Long id;
    private final String correo;

    public UsuarioSesion(String rol, Long id, String correo) {
        this.rol = rol;
        this.id = id;
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return null; //la contrasena nunca sale de la base de datos
    }

    @Override
    public String getUsername() {
        return correo;
    }
}
