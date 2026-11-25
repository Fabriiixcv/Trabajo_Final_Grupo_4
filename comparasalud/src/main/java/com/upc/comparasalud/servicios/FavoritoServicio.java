package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.FavoritoDTO;
import com.upc.comparasalud.dto.IdDTO;
import com.upc.comparasalud.dto.MedicamentoPrecioDTO;
import com.upc.comparasalud.dto.UbicacionDTO;
import com.upc.comparasalud.entidades.*;
import com.upc.comparasalud.excepciones.ConflictoException;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FavoritoServicio {
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private MedicamentoRepositorio medicamentoRepositorio;
    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;
    @Autowired
    private FavoritoRepositorio favoritoRepositorio;
    @Autowired
    private FarmaciaFavoritaRepositorio farmaciaFavoritaRepositorio;
    @Autowired
    private MedicamentoServicio medicamentoServicio;

    private Cliente buscarCliente(Long id) {
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con el id: " + id));
    }

    //HU03
    @Transactional
    public IdDTO agregarMedicamento(Long idCliente, FavoritoDTO dto) {
        Cliente cliente = buscarCliente(idCliente);
        if (dto.getIdMedicamento() == null)
            throw new ReglaNegocioException("El idMedicamento es obligatorio");
        Medicamento medicamento = medicamentoRepositorio.findById(dto.getIdMedicamento())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el medicamento con el id: " + dto.getIdMedicamento()));
        if (favoritoRepositorio.existsByCliente_IdAndMedicamento_Id(idCliente, medicamento.getId()))
            throw new ConflictoException("El medicamento ya esta en favoritos");
        Favorito favorito = new Favorito();
        favorito.setCliente(cliente);
        favorito.setMedicamento(medicamento);
        favoritoRepositorio.save(favorito);
        return new IdDTO(medicamento.getId());
    }

    //HU18
    @Transactional
    public void quitarMedicamento(Long idCliente, Long idMedicamento) {
        buscarCliente(idCliente);
        Favorito favorito = favoritoRepositorio.findByCliente_IdAndMedicamento_Id(idCliente, idMedicamento)
                .orElseThrow(() -> new RecursoNoEncontradoException("El medicamento no esta en favoritos"));
        favoritoRepositorio.delete(favorito);
    }

    //HU19
    public List<MedicamentoPrecioDTO> listarMedicamentos(Long idCliente) {
        buscarCliente(idCliente);
        Map<Long, BigDecimal> precios = medicamentoServicio.preciosDesde(false);
        List<MedicamentoPrecioDTO> lista = new ArrayList<>();
        for (Favorito f : favoritoRepositorio.findByCliente_Id(idCliente)) {
            Medicamento m = f.getMedicamento();
            lista.add(new MedicamentoPrecioDTO(m.getId(), m.getNombre(), m.getNombreComercial(),
                    m.getCategoria() != null ? m.getCategoria().getNombre() : null, precios.get(m.getId())));
        }
        return lista;
    }

    //HU28
    @Transactional
    public IdDTO agregarFarmacia(Long idCliente, FavoritoDTO dto) {
        Cliente cliente = buscarCliente(idCliente);
        if (dto.getIdFarmacia() == null)
            throw new ReglaNegocioException("El idFarmacia es obligatorio");
        Farmacia farmacia = farmaciaRepositorio.findByIdAndEstado(dto.getIdFarmacia(), "ACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la farmacia con el id: " + dto.getIdFarmacia()));
        if (farmaciaFavoritaRepositorio.existsByCliente_IdAndFarmacia_Id(idCliente, farmacia.getId()))
            throw new ConflictoException("La farmacia ya esta en favoritos");
        FarmaciaFavorita favorita = new FarmaciaFavorita();
        favorita.setCliente(cliente);
        favorita.setFarmacia(farmacia);
        farmaciaFavoritaRepositorio.save(favorita);
        return new IdDTO(farmacia.getId());
    }

    //HU51
    @Transactional
    public void quitarFarmacia(Long idCliente, Long idFarmacia) {
        buscarCliente(idCliente);
        FarmaciaFavorita favorita = farmaciaFavoritaRepositorio.findByCliente_IdAndFarmacia_Id(idCliente, idFarmacia)
                .orElseThrow(() -> new RecursoNoEncontradoException("La farmacia no esta en favoritos"));
        farmaciaFavoritaRepositorio.delete(favorita);
    }

    //HU20: solo las farmacias que siguen activas
    public List<UbicacionDTO> listarFarmacias(Long idCliente) {
        buscarCliente(idCliente);
        List<UbicacionDTO> lista = new ArrayList<>();
        for (FarmaciaFavorita f : farmaciaFavoritaRepositorio.findByCliente_Id(idCliente)) {
            Farmacia farmacia = f.getFarmacia();
            if (!"ACTIVA".equals(farmacia.getEstado())) continue;
            lista.add(new UbicacionDTO(farmacia.getId(), farmacia.getNombre(),
                    farmacia.getDireccion(), farmacia.getDistrito()));
        }
        return lista;
    }
}
