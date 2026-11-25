package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.OfertaDTO;
import com.upc.comparasalud.dto.PrecioStockDTO;
import com.upc.comparasalud.dto.ReporteComparacionDTO;
import com.upc.comparasalud.entidades.Farmacia;
import com.upc.comparasalud.entidades.Medicamento;
import com.upc.comparasalud.entidades.OfertaMedicamento;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.FarmaciaRepositorio;
import com.upc.comparasalud.repositorios.MedicamentoRepositorio;
import com.upc.comparasalud.repositorios.OfertaRepositorio;
import com.upc.comparasalud.excepciones.AccesoDenegadoException;
import com.upc.comparasalud.security.SesionActual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfertaServicio {
    @Autowired
    private SesionActual sesion;
    @Autowired
    private OfertaRepositorio ofertaRepositorio;
    @Autowired
    private MedicamentoRepositorio medicamentoRepositorio;
    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;

    public OfertaDTO aDTO(OfertaMedicamento o) {
        OfertaDTO dto = new OfertaDTO();
        dto.setId(o.getId());
        dto.setIdMedicamento(o.getMedicamento().getId());
        dto.setNombreMedicamento(o.getMedicamento().getNombre());
        dto.setIdFarmacia(o.getFarmacia().getId());
        dto.setNombreFarmacia(o.getFarmacia().getNombre());
        dto.setDistrito(o.getFarmacia().getDistrito());
        dto.setPrecio(o.getPrecio());
        dto.setStock(o.getStock());
        dto.setFechaActualizacion(o.getFechaActualizacion());
        return dto;
    }

    private List<OfertaDTO> aDTO(List<OfertaMedicamento> ofertas) {
        return ofertas.stream().map(this::aDTO).collect(Collectors.toList());
    }

    private OfertaMedicamento buscar(Long id) {
        return ofertaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la oferta con el id: " + id));
    }

    //una farmacia solo modifica o elimina sus propias ofertas
    private OfertaMedicamento buscarPropia(Long id) {
        OfertaMedicamento oferta = buscar(id);
        if (!sesion.esFarmacia(oferta.getFarmacia().getId()))
            throw new AccesoDenegadoException(SesionActual.DENEGADO);
        return oferta;
    }

    private Medicamento buscarMedicamento(Long id) {
        if (id == null) throw new ReglaNegocioException("El idMedicamento es obligatorio");
        return medicamentoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el medicamento con el id: " + id));
    }

    //HU33: el precio debe ser mayor que 0; se guarda con dos decimales
    private BigDecimal validarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new ReglaNegocioException("El precio debe ser mayor que 0");
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    //en actualizaciones el stock puede ser 0 (oferta agotada), pero no negativo
    private void validarStock(Integer stock) {
        if (stock == null)
            throw new ReglaNegocioException("El stock es obligatorio");
        if (stock < 0)
            throw new ReglaNegocioException("El stock no puede ser negativo");
    }

    //HU42: farmacia activa y stock mayor que 0
    @Transactional
    public OfertaDTO insertar(OfertaDTO dto) {
        Medicamento medicamento = buscarMedicamento(dto.getIdMedicamento());
        if (dto.getIdFarmacia() == null)
            throw new ReglaNegocioException("El idFarmacia es obligatorio");
        sesion.exigirFarmacia(dto.getIdFarmacia()); //solo puede publicar ofertas de su propia farmacia
        Farmacia farmacia = farmaciaRepositorio.findById(dto.getIdFarmacia())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la farmacia con el id: " + dto.getIdFarmacia()));
        if (!"ACTIVA".equals(farmacia.getEstado()))
            throw new ReglaNegocioException("La farmacia esta inactiva, no se pueden registrar ofertas");
        BigDecimal precio = validarPrecio(dto.getPrecio());
        if (dto.getStock() == null || dto.getStock() <= 0)
            throw new ReglaNegocioException("El stock debe ser mayor que 0 para registrar la oferta");
        if (ofertaRepositorio.existsByMedicamento_IdAndFarmacia_Id(medicamento.getId(), farmacia.getId()))
            throw new ReglaNegocioException("La farmacia ya tiene una oferta para este medicamento");
        OfertaMedicamento oferta = new OfertaMedicamento();
        oferta.setMedicamento(medicamento);
        oferta.setFarmacia(farmacia);
        oferta.setPrecio(precio);
        oferta.setStock(dto.getStock());
        oferta.setFechaActualizacion(LocalDateTime.now());
        return aDTO(ofertaRepositorio.save(oferta));
    }

    //HU29: precio y stock obligatorios
    @Transactional
    public OfertaDTO editar(Long id, PrecioStockDTO dto) {
        OfertaMedicamento oferta = buscarPropia(id);
        BigDecimal precio = validarPrecio(dto.getPrecio());
        validarStock(dto.getStock());
        oferta.setPrecio(precio);
        oferta.setStock(dto.getStock());
        oferta.setFechaActualizacion(LocalDateTime.now());
        return aDTO(ofertaRepositorio.save(oferta));
    }

    //HU45: modificacion parcial de precio y/o stock (al menos uno)
    @Transactional
    public OfertaDTO modificarParcial(Long id, PrecioStockDTO dto) {
        OfertaMedicamento oferta = buscarPropia(id);
        if (dto.getPrecio() == null && dto.getStock() == null)
            throw new ReglaNegocioException("Debe enviar el precio y/o el stock");
        if (dto.getPrecio() != null) oferta.setPrecio(validarPrecio(dto.getPrecio()));
        if (dto.getStock() != null) {
            validarStock(dto.getStock());
            oferta.setStock(dto.getStock());
        }
        oferta.setFechaActualizacion(LocalDateTime.now());
        return aDTO(ofertaRepositorio.save(oferta));
    }

    //HU31: solo se elimina la oferta
    @Transactional
    public void eliminar(Long id) {
        buscarPropia(id);
        ofertaRepositorio.deleteById(id);
    }

    //HU58: ofertas que publican las farmacias activas (solo con stock); filtros opcionales por farmacia y distrito
    public List<OfertaDTO> listarDisponibles(Long idFarmacia, String distrito) {
        if (idFarmacia != null)
            farmaciaRepositorio.findByIdAndEstado(idFarmacia, "ACTIVA")
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe la farmacia con el id: " + idFarmacia));
        return ofertaRepositorio.findByFarmacia_Estado("ACTIVA").stream()
                .filter(o -> o.getStock() > 0)
                .filter(o -> idFarmacia == null || idFarmacia.equals(o.getFarmacia().getId()))
                .filter(o -> distrito == null || distrito.isBlank()
                        || o.getFarmacia().getDistrito().equalsIgnoreCase(distrito.trim()))
                .sorted(Comparator.comparing(OfertaMedicamento::getFechaActualizacion, Comparator.reverseOrder()))
                .map(this::aDTO)
                .collect(Collectors.toList());
    }

    //HU43: todas las ofertas de la farmacia, incluidas las de stock 0
    public List<OfertaDTO> listarPorFarmacia(Long idFarmacia) {
        farmaciaRepositorio.findByIdAndEstado(idFarmacia, "ACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la farmacia con el id: " + idFarmacia));
        return aDTO(ofertaRepositorio.findByFarmacia_Id(idFarmacia));
    }

    //HU48: reporte de comparacion de precios (solo farmacias activas)
    public ReporteComparacionDTO generarReporte(Long idMedicamento) {
        Medicamento medicamento = buscarMedicamento(idMedicamento);
        List<OfertaDTO> ofertas = aDTO(
                ofertaRepositorio.findByMedicamento_IdAndFarmacia_EstadoOrderByPrecioAsc(idMedicamento, "ACTIVA"));
        ReporteComparacionDTO reporte = new ReporteComparacionDTO();
        reporte.setIdMedicamento(medicamento.getId());
        reporte.setMedicamento(medicamento.getNombre());
        reporte.setTotalOfertas(ofertas.size());
        reporte.setOfertas(ofertas);
        if (!ofertas.isEmpty()) {
            BigDecimal suma = BigDecimal.ZERO;
            for (OfertaDTO o : ofertas) suma = suma.add(o.getPrecio());
            reporte.setPrecioMinimo(ofertas.get(0).getPrecio());
            reporte.setPrecioMaximo(ofertas.get(ofertas.size() - 1).getPrecio());
            reporte.setPrecioPromedio(suma.divide(BigDecimal.valueOf(ofertas.size()), 2, RoundingMode.HALF_UP));
            reporte.setOfertaMasEconomica(ofertas.get(0));
        }
        return reporte;
    }
}
