package com.upc.comparasalud.repositorios;

import com.upc.comparasalud.entidades.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepositorio extends JpaRepository<Favorito, Long> {
    List<Favorito> findByCliente_Id(Long idCliente);
    Optional<Favorito> findByCliente_IdAndMedicamento_Id(Long idCliente, Long idMedicamento);
    boolean existsByCliente_IdAndMedicamento_Id(Long idCliente, Long idMedicamento);
    void deleteByCliente_Id(Long idCliente);
    void deleteByMedicamento_Id(Long idMedicamento);
}
