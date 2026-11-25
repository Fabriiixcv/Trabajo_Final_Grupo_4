package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.FarmaciaFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmaciaFavoritaRepositorio extends JpaRepository<FarmaciaFavorita, Long> {
    List<FarmaciaFavorita> findByCliente_Id(Long idCliente);
    Optional<FarmaciaFavorita> findByCliente_IdAndFarmacia_Id(Long idCliente, Long idFarmacia);
    boolean existsByCliente_IdAndFarmacia_Id(Long idCliente, Long idFarmacia);
    void deleteByCliente_Id(Long idCliente);
}
