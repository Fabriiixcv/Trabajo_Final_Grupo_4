package com.upc.comparasalud.controladores;

import com.upc.comparasalud.dto.OfertaDTO;
import com.upc.comparasalud.dto.PrecioStockDTO;
import com.upc.comparasalud.servicios.OfertaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OfertaController {
    @Autowired
    private OfertaServicio ofertaServicio;

    //HU42 (con la validacion de precio de HU33)
    @PostMapping("/ofertas")
    public OfertaDTO insertar(@RequestBody OfertaDTO ofertaDTO) {
        return ofertaServicio.insertar(ofertaDTO);
    }
    //HU58: ofertas de las distintas farmacias (?farmaciaId=&distrito= opcionales)
    @GetMapping("/ofertas")
    public List<OfertaDTO> listarDisponibles(@RequestParam(required = false) Long farmaciaId,
                                             @RequestParam(required = false) String distrito) {
        return ofertaServicio.listarDisponibles(farmaciaId, distrito);
    }
    //HU29
    @PutMapping("/ofertas/{id}")
    public OfertaDTO editar(@PathVariable Long id, @RequestBody PrecioStockDTO precioStockDTO) {
        return ofertaServicio.editar(id, precioStockDTO);
    }
    //HU45 (PUT segun la lista; al ser parcial, PATCH seria lo mas correcto: cambiar @PutMapping por @PatchMapping)
    @PutMapping("/ofertas/{id}/precio-stock")
    public OfertaDTO modificarParcial(@PathVariable Long id, @RequestBody PrecioStockDTO precioStockDTO) {
        return ofertaServicio.modificarParcial(id, precioStockDTO);
    }
    //HU31
    @DeleteMapping("/ofertas/{id}")
    public void eliminar(@PathVariable Long id) {
        ofertaServicio.eliminar(id);
    }
}
