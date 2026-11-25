package com.upc.comparasalud.config;

import com.upc.comparasalud.entidades.Administrador;
import com.upc.comparasalud.repositorios.AdministradorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//El administrador es un usuario fijo: no se registra por la API, se crea aqui si la tabla esta vacia.
//Credenciales iniciales: admin@comparasalud.com / admin123
@Component
public class DatosIniciales implements CommandLineRunner {
    @Autowired
    private AdministradorRepositorio administradorRepositorio;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (administradorRepositorio.count() == 0) {
            Administrador administrador = new Administrador();
            administrador.setNombre("Administrador");
            administrador.setCorreo("admin@comparasalud.com");
            administrador.setContrasena(passwordEncoder.encode("admin123"));
            administradorRepositorio.save(administrador);
        }
    }
}
