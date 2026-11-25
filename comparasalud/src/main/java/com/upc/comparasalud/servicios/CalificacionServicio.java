package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.CalificacionDTO;
import com.upc.comparasalud.dto.CalificacionDetalleDTO;
import com.upc.comparasalud.dto.ResumenCalificacionesDTO;
import com.upc.comparasalud.entidades.Calificacion;
import com.upc.comparasalud.entidades.Cliente;
import com.upc.comparasalud.entidades.Farmacia;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.CalificacionRepositorio;
import com.upc.comparasalud.repositorios.ClienteRepositorio;
import com.upc.comparasalud.repositorios.FarmaciaRepositorio;
import com.upc.comparasalud.security.SesionActual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalificacionServicio {
    @Autowired
    private SesionActual sesion;
    @Autowired
    private CalificacionRepositorio calificacionRepositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;

    private Farmacia buscarFarmaciaActiva(Long id) {
        return farmaciaRepositorio.findByIdAndEstado(id, "ACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la farmacia con el id: " + id));
    }

    //HU47
    @Transactional
    public CalificacionDTO registrar(Long idFarmacia, CalificacionDTO dto) {
        Farmacia farmacia = buscarFarmaciaActiva(idFarmacia);
        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5)
            throw new ReglaNegocioException("La puntuacion debe estar entre 1 y 5");
        if (dto.getIdCliente() == null)
            throw new ReglaNegocioException("El idCliente es obligatorio");
        sesion.exigirCliente(dto.getIdCliente()); //solo puede calificar con su propia cuenta
        Cliente cliente = clienteRepositorio.findById(dto.getIdCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con el id: " + dto.getIdCliente()));
        Calificacion calificacion = new Calificacion();
        calificacion.setCliente(cliente);
        calificacion.setFarmacia(farmacia);
        calificacion.setPuntuacion(dto.getPuntuacion());
        calificacion.setComentario(dto.getComentario());
        calificacion.setFechaCreacion(LocalDateTime.now());
        Calificacion guardada = calificacionRepositorio.save(calificacion);
        return new CalificacionDTO(guardada.getId(), cliente.getId(), farmacia.getId(),
                guardada.getPuntuacion(), guardada.getComentario(), guardada.getFechaCreacion());
    }

    //HU27: total, promedio y lista (de la mas reciente a la mas antigua)
    public ResumenCalificacionesDTO listar(Long idFarmacia) {
        buscarFarmaciaActiva(idFarmacia);
        List<Calificacion> calificaciones = calificacionRepositorio.findByFarmacia_IdOrderByFechaCreacionDesc(idFarmacia);
        List<CalificacionDetalleDTO> detalle = new ArrayList<>();
        int suma = 0;
        for (Calificacion c : calificaciones) {
            suma += c.getPuntuacion();
            detalle.add(new CalificacionDetalleDTO(
                    c.getCliente().getNombre() + " " + c.getCliente().getApellidos(),
                    c.getPuntuacion(), c.getComentario(), c.getFechaCreacion()));
        }
        BigDecimal promedio = calificaciones.isEmpty() ? null
                : BigDecimal.valueOf(suma).divide(BigDecimal.valueOf(calificaciones.size()), 2, RoundingMode.HALF_UP);
        return new ResumenCalificacionesDTO(calificaciones.size(), promedio, detalle);
    }
}
