package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    boolean existsByCorreo(String correo);
    Optional<Cliente> findByCorreo(String correo);
}
