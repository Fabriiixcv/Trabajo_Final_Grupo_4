package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.OfertaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfertaRepositorio extends JpaRepository<OfertaMedicamento, Long> {
    List<OfertaMedicamento> findByFarmacia_Id(Long idFarmacia);
    List<OfertaMedicamento> findByFarmacia_Estado(String estado);
    List<OfertaMedicamento> findByMedicamento_IdAndFarmacia_EstadoOrderByPrecioAsc(Long idMedicamento, String estado);
    List<OfertaMedicamento> findByMedicamento_IdIn(List<Long> ids);
    boolean existsByMedicamento_IdAndFarmacia_Id(Long idMedicamento, Long idFarmacia);
    void deleteByMedicamento_Id(Long idMedicamento);
    long countByFarmacia_Estado(String estado);
}
