package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.*;
import com.upc.comparasalud.entidades.OfertaMedicamento;
import com.upc.comparasalud.repositorios.OfertaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//Panel del administrador: cada consulta devuelve el total y el detalle de los registros
@Service
public class AdministradorServicio {
    @Autowired
    private FarmaciaServicio farmaciaServicio;
    @Autowired
    private OfertaServicio ofertaServicio;
    @Autowired
    private OfertaRepositorio ofertaRepositorio;
    @Autowired
    private ClienteServicio clienteServicio;
    @Autowired
    private CategoriaServicio categoriaServicio;
    @Autowired
    private MedicamentoServicio medicamentoServicio;

    private <T> ListadoDTO<T> listado(List<T> detalle) {
        return new ListadoDTO<>(detalle.size(), detalle);
    }

    //HU34: solo farmacias activas
    public ListadoDTO<FarmaciaDTO> farmacias() {
        return listado(farmaciaServicio.listarActivasParaAdministrador());
    }

    //HU36: ofertas de farmacias activas, incluidas las de stock 0
    public ListadoDTO<OfertaDTO> ofertas() {
        List<OfertaDTO> ofertas = ofertaRepositorio.findByFarmacia_Estado("ACTIVA").stream()
                .sorted(Comparator.comparing((OfertaMedicamento o) -> o.getFarmacia().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(o -> o.getMedicamento().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(ofertaServicio::aDTO)
                .collect(Collectors.toList());
        return listado(ofertas);
    }

    //HU37
    public ListadoDTO<ClienteDTO> clientes() {
        return listado(clienteServicio.listar());
    }

    //HU46
    public ListadoDTO<CategoriaDTO> categorias() {
        return listado(categoriaServicio.listar());
    }

    //HU50
    public ListadoDTO<MedicamentoDTO> medicamentos() {
        return listado(medicamentoServicio.listarTodos());
    }

    //HU56: el administrador elimina un cliente (borrado fisico: sus favoritos, calificaciones y la cuenta)
    public void eliminarCliente(Long id) {
        clienteServicio.eliminar(id);
    }

    //HU57: el administrador da de baja una farmacia (baja logica: pasa a INACTIVA)
    public void eliminarFarmacia(Long id) {
        farmaciaServicio.eliminar(id);
    }
}
