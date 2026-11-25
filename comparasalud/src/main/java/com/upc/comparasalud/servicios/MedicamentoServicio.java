package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.*;
import com.upc.comparasalud.entidades.Categoria;
import com.upc.comparasalud.entidades.Farmacia;
import com.upc.comparasalud.entidades.Medicamento;
import com.upc.comparasalud.entidades.OfertaMedicamento;
import com.upc.comparasalud.excepciones.AccesoDenegadoException;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.*;
import com.upc.comparasalud.security.SesionActual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.upc.comparasalud.util.Validaciones.vacio;

@Service
public class MedicamentoServicio {
    @Autowired
    private SesionActual sesion;
    @Autowired
    private MedicamentoRepositorio medicamentoRepositorio;
    @Autowired
    private CategoriaRepositorio categoriaRepositorio;
    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;
    @Autowired
    private OfertaRepositorio ofertaRepositorio;
    @Autowired
    private FavoritoRepositorio favoritoRepositorio;
    @Autowired
    private OfertaServicio ofertaServicio;

    private MedicamentoDTO aDTO(Medicamento m) {
        MedicamentoDTO dto = new MedicamentoDTO();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setNombreComercial(m.getNombreComercial());
        dto.setPrincipioActivo(m.getPrincipioActivo());
        dto.setConcentracion(m.getConcentracion());
        dto.setPresentacion(m.getPresentacion());
        dto.setLaboratorio(m.getLaboratorio());
        dto.setDescripcion(m.getDescripcion());
        dto.setAdvertencias(m.getAdvertencias());
        if (m.getCategoria() != null) {
            dto.setIdCategoria(m.getCategoria().getId());
            dto.setNombreCategoria(m.getCategoria().getNombre());
        }
        if (m.getRegistradoPor() != null)
            dto.setIdFarmacia(m.getRegistradoPor().getId());
        return dto;
    }

    private Medicamento buscar(Long id) {
        return medicamentoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el medicamento con el id: " + id));
    }

    private Categoria buscarCategoria(Long idCategoria) {
        if (idCategoria == null) return null;
        return categoriaRepositorio.findById(idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoria con el id: " + idCategoria));
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase();
    }

    //no se puede repetir un medicamento con el mismo nombre, concentracion, presentacion y laboratorio
    private void validarDuplicado(MedicamentoDTO dto, Long idExcluido) {
        for (Medicamento m : medicamentoRepositorio.findByNombreIgnoreCase(dto.getNombre().trim())) {
            if (idExcluido != null && idExcluido.equals(m.getId())) continue;
            if (normalizar(m.getConcentracion()).equals(normalizar(dto.getConcentracion()))
                    && normalizar(m.getPresentacion()).equals(normalizar(dto.getPresentacion()))
                    && normalizar(m.getLaboratorio()).equals(normalizar(dto.getLaboratorio())))
                throw new ReglaNegocioException("Ya existe el medicamento: " + dto.getNombre());
        }
    }

    private void aplicar(Medicamento m, MedicamentoDTO dto, Categoria categoria) {
        m.setNombre(dto.getNombre().trim());
        m.setNombreComercial(dto.getNombreComercial());
        m.setPrincipioActivo(dto.getPrincipioActivo());
        m.setConcentracion(dto.getConcentracion());
        m.setPresentacion(dto.getPresentacion());
        m.setLaboratorio(dto.getLaboratorio());
        m.setDescripcion(dto.getDescripcion());
        m.setAdvertencias(dto.getAdvertencias());
        m.setCategoria(categoria);
    }

    //precio mas bajo por medicamento, solo con ofertas de farmacias ACTIVAS
    public Map<Long, BigDecimal> preciosDesde(boolean soloConStock) {
        return minimos(ofertaRepositorio.findByFarmacia_Estado("ACTIVA"), soloConStock);
    }

    private Map<Long, BigDecimal> minimos(List<OfertaMedicamento> ofertas, boolean soloConStock) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        for (OfertaMedicamento o : ofertas) {
            if (!"ACTIVA".equals(o.getFarmacia().getEstado())) continue;
            if (soloConStock && o.getStock() <= 0) continue;
            mapa.merge(o.getMedicamento().getId(), o.getPrecio(), BigDecimal::min);
        }
        return mapa;
    }

    //HU22: guarda la farmacia que lo registro
    @Transactional
    public MedicamentoDTO insertar(MedicamentoDTO dto) {
        sesion.exigirFarmacia(dto.getIdFarmacia()); //solo puede registrar a nombre de su propia farmacia
        if (vacio(dto.getNombre()))
            throw new ReglaNegocioException("El nombre del medicamento es obligatorio");
        if (dto.getIdFarmacia() == null)
            throw new ReglaNegocioException("El idFarmacia es obligatorio");
        Farmacia farmacia = farmaciaRepositorio.findByIdAndEstado(dto.getIdFarmacia(), "ACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la farmacia con el id: " + dto.getIdFarmacia()));
        Categoria categoria = buscarCategoria(dto.getIdCategoria());
        validarDuplicado(dto, null);
        Medicamento medicamento = new Medicamento();
        aplicar(medicamento, dto, categoria);
        medicamento.setRegistradoPor(farmacia);
        return aDTO(medicamentoRepositorio.save(medicamento));
    }

    //HU50 (panel del administrador): todo el catalogo, tengan o no ofertas
    public List<MedicamentoDTO> listarTodos() {
        return medicamentoRepositorio.findAll().stream()
                .sorted(Comparator.comparing(Medicamento::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::aDTO)
                .collect(Collectors.toList());
    }

    //HU23
    public MedicamentoDTO buscarPorId(Long id) {
        return aDTO(buscar(id));
    }

    //HU30: solo la farmacia que lo registro puede modificarlo
    @Transactional
    public MedicamentoDTO editar(Long id, MedicamentoDTO dto) {
        Medicamento medicamento = buscar(id);
        //la farmacia que modifica sale de la sesion (el idFarmacia del cuerpo, si llega, debe ser el mismo)
        sesion.exigirFarmacia(dto.getIdFarmacia());
        if (medicamento.getRegistradoPor() == null || !sesion.esFarmacia(medicamento.getRegistradoPor().getId()))
            throw new AccesoDenegadoException("Solo puede modificar los medicamentos que registro");
        if (vacio(dto.getNombre()))
            throw new ReglaNegocioException("El nombre del medicamento es obligatorio");
        //la categoria puede cambiarse o quitarse (sin idCategoria queda sin categoria)
        Categoria categoria = buscarCategoria(dto.getIdCategoria());
        validarDuplicado(dto, id);
        aplicar(medicamento, dto, categoria);
        return aDTO(medicamentoRepositorio.save(medicamento));
    }

    //HU32: elimina primero sus ofertas y favoritos
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        ofertaRepositorio.deleteByMedicamento_Id(id);
        favoritoRepositorio.deleteByMedicamento_Id(id);
        medicamentoRepositorio.deleteById(id);
    }

    //HU35: busqueda parcial por nombre, nombre comercial o principio activo, con las farmacias activas que tienen stock
    public List<BusquedaMedicamentoDTO> buscarPorNombre(String nombre) {
        if (vacio(nombre))
            throw new ReglaNegocioException("Debe indicar el nombre del medicamento a buscar");
        String texto = normalizar(nombre);
        List<Medicamento> encontrados = medicamentoRepositorio.findAll().stream()
                .filter(m -> normalizar(m.getNombre()).contains(texto)
                        || normalizar(m.getNombreComercial()).contains(texto)
                        || normalizar(m.getPrincipioActivo()).contains(texto))
                .sorted(Comparator.comparing(Medicamento::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        if (encontrados.isEmpty()) return List.of();
        List<Long> ids = encontrados.stream().map(Medicamento::getId).collect(Collectors.toList());
        Map<Long, List<OfertaDTO>> porMedicamento = new HashMap<>();
        ofertaRepositorio.findByMedicamento_IdIn(ids).stream()
                .filter(o -> "ACTIVA".equals(o.getFarmacia().getEstado()) && o.getStock() > 0)
                .sorted(Comparator.comparing(OfertaMedicamento::getPrecio))
                .forEach(o -> porMedicamento
                        .computeIfAbsent(o.getMedicamento().getId(), k -> new ArrayList<>())
                        .add(ofertaServicio.aDTO(o)));
        List<BusquedaMedicamentoDTO> resultado = new ArrayList<>();
        for (Medicamento m : encontrados)
            resultado.add(new BusquedaMedicamentoDTO(m.getId(), m.getNombre(), m.getNombreComercial(),
                    porMedicamento.getOrDefault(m.getId(), List.of())));
        return resultado;
    }

    //HU26: oferta mas barata y ofertas de cada medicamento (ids = dos o mas, separados por coma)
    public List<ComparacionMedicamentoDTO> comparar(List<Long> ids) {
        List<Long> distintos = ids == null ? List.of() : ids.stream().distinct().collect(Collectors.toList());
        if (distintos.size() < 2)
            throw new ReglaNegocioException("Debe indicar al menos dos medicamentos para comparar");
        List<Medicamento> medicamentos = new ArrayList<>();
        for (Long id : distintos) medicamentos.add(buscar(id));
        medicamentos.sort(Comparator.comparing(Medicamento::getNombre, String.CASE_INSENSITIVE_ORDER));
        Map<Long, List<OfertaDTO>> porMedicamento = new HashMap<>();
        ofertaRepositorio.findByMedicamento_IdIn(distintos).stream()
                .filter(o -> "ACTIVA".equals(o.getFarmacia().getEstado()))
                .sorted(Comparator.comparing(OfertaMedicamento::getPrecio))
                .forEach(o -> porMedicamento
                        .computeIfAbsent(o.getMedicamento().getId(), k -> new ArrayList<>())
                        .add(ofertaServicio.aDTO(o)));
        List<ComparacionMedicamentoDTO> resultado = new ArrayList<>();
        for (Medicamento m : medicamentos) {
            List<OfertaDTO> ofertas = porMedicamento.getOrDefault(m.getId(), List.of());
            resultado.add(new ComparacionMedicamentoDTO(m.getId(), m.getNombre(), m.getNombreComercial(),
                    ofertas.isEmpty() ? null : ofertas.get(0), ofertas));
        }
        return resultado;
    }

    //HU49: mismo principio activo o misma categoria, con stock en farmacias activas, del mas barato al mas caro
    public List<RecomendacionDTO> recomendaciones(Long id) {
        Medicamento m = buscar(id);
        Set<Long> candidatos = new LinkedHashSet<>();
        if (!vacio(m.getPrincipioActivo()))
            for (Medicamento alt : medicamentoRepositorio.findByPrincipioActivoIgnoreCaseAndIdNot(m.getPrincipioActivo(), id))
                candidatos.add(alt.getId());
        if (m.getCategoria() != null)
            for (Medicamento alt : medicamentoRepositorio.findByCategoria_IdAndIdNot(m.getCategoria().getId(), id))
                candidatos.add(alt.getId());
        if (candidatos.isEmpty()) return List.of();
        List<OfertaMedicamento> ofertas = ofertaRepositorio.findByMedicamento_IdIn(new ArrayList<>(candidatos));
        Map<Long, BigDecimal> precios = minimos(ofertas, true);
        Map<Long, Medicamento> datos = new HashMap<>();
        for (OfertaMedicamento o : ofertas) datos.put(o.getMedicamento().getId(), o.getMedicamento());
        List<RecomendacionDTO> lista = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : precios.entrySet()) {
            Medicamento alt = datos.get(e.getKey());
            lista.add(new RecomendacionDTO(alt.getId(), alt.getNombre(), alt.getNombreComercial(), e.getValue()));
        }
        lista.sort(Comparator.comparing(RecomendacionDTO::getPrecioMinimo));
        return lista;
    }
}
