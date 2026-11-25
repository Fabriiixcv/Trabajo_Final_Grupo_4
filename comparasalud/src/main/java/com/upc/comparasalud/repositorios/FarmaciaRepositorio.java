package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.Farmacia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmaciaRepositorio extends JpaRepository<Farmacia, Long> {
    boolean existsByCorreo(String correo);
    boolean existsByLicencia(String licencia);
    boolean existsByLicenciaAndIdNot(String licencia, Long id);
    Optional<Farmacia> findByCorreo(String correo);
    Optional<Farmacia> findByIdAndEstado(Long id, String estado);
    List<Farmacia> findByEstado(String estado);
    long countByEstado(String estado);
}
