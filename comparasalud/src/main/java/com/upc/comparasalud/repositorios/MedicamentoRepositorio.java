package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepositorio extends JpaRepository<Medicamento, Long> {
    //medicamentos con el mismo nombre (para detectar duplicados)
    List<Medicamento> findByNombreIgnoreCase(String nombre);
    //alternativas: mismo principio activo, excluyendo al propio medicamento
    List<Medicamento> findByPrincipioActivoIgnoreCaseAndIdNot(String principioActivo, Long id);
    //medicamentos de la misma categoria, excluyendo al propio medicamento
    List<Medicamento> findByCategoria_IdAndIdNot(Long idCategoria, Long id);
    //saber si una categoria tiene medicamentos
    boolean existsByCategoria_Id(Long idCategoria);
}
