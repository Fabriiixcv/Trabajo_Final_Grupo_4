package com.upc.comparasalud.controladores;

import com.upc.comparasalud.dto.*;
import com.upc.comparasalud.servicios.MedicamentoServicio;
import com.upc.comparasalud.servicios.OfertaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicamentoController {
    @Autowired
    private MedicamentoServicio medicamentoServicio;
    @Autowired
    private OfertaServicio ofertaServicio;

    //HU22
    @PostMapping("/medicamentos")
    public MedicamentoDTO insertar(@RequestBody MedicamentoDTO medicamentoDTO) {
        return medicamentoServicio.insertar(medicamentoDTO);
    }
    //HU35 (buscar por nombre y comparar precios por farmacia)
    @GetMapping("/medicamentos/buscar")
    public List<BusquedaMedicamentoDTO> buscarPorNombre(@RequestParam(required = false) String nombre) {
        return medicamentoServicio.buscarPorNombre(nombre);
    }
    //HU26 (ej: /api/medicamentos/comparacion?ids=1,2)
    @GetMapping("/medicamentos/comparacion")
    public List<ComparacionMedicamentoDTO> comparar(@RequestParam(required = false) List<Long> ids) {
        return medicamentoServicio.comparar(ids);
    }
    //HU23
    @GetMapping("/medicamentos/{id}")
    public MedicamentoDTO buscarPorId(@PathVariable Long id) {
        return medicamentoServicio.buscarPorId(id);
    }
    //HU49
    @GetMapping("/medicamentos/{id}/recomendaciones")
    public List<RecomendacionDTO> recomendaciones(@PathVariable Long id) {
        return medicamentoServicio.recomendaciones(id);
    }
    //HU48
    @GetMapping("/medicamentos/{id}/reporte-precios")
    public ReporteComparacionDTO reportePrecios(@PathVariable Long id) {
        return ofertaServicio.generarReporte(id);
    }
    //HU30
    @PutMapping("/medicamentos/{id}")
    public MedicamentoDTO editar(@PathVariable Long id, @RequestBody MedicamentoDTO medicamentoDTO) {
        return medicamentoServicio.editar(id, medicamentoDTO);
    }
    //HU32
    @DeleteMapping("/medicamentos/{id}")
    public void eliminar(@PathVariable Long id) {
        medicamentoServicio.eliminar(id);
    }
}
