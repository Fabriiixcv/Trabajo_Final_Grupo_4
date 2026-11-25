package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepositorio extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByFarmacia_IdOrderByFechaCreacionDesc(Long idFarmacia);
    void deleteByCliente_Id(Long idCliente);
}
