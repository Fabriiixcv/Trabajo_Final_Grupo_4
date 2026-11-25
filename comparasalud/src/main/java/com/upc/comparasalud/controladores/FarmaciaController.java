package com.upc.comparasalud.controladores;

import com.upc.comparasalud.dto.*;
import com.upc.comparasalud.servicios.AuthServicio;
import com.upc.comparasalud.servicios.CalificacionServicio;
import com.upc.comparasalud.servicios.FarmaciaServicio;
import com.upc.comparasalud.servicios.OfertaServicio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") //prefijo
@Slf4j
public class FarmaciaController {
    @Autowired
    private FarmaciaServicio farmaciaServicio;
    @Autowired
    private OfertaServicio ofertaServicio;
    @Autowired
    private CalificacionServicio calificacionServicio;
    @Autowired
    private AuthServicio authServicio;

    //HU09 (con las validaciones de HU08)
    @PostMapping("/farmacias")
    public FarmaciaDTO insertar(@RequestBody FarmaciaDTO farmaciaDTO) {
        return farmaciaServicio.insertar(farmaciaDTO);
    }
    //HU53
    @PostMapping("/farmacias/login")
    public SesionDTO login(@RequestBody LoginDTO loginDTO) {
        return authServicio.loginFarmacia(loginDTO);
    }
    //HU12 (?distrito=), HU13 (?nombre=) y HU14 (?estado=activa): un solo metodo para la ruta
    @GetMapping("/farmacias")
    public List<FarmaciaDTO> listar(@RequestParam(required = false) String distrito,
                                    @RequestParam(required = false) String nombre,
                                    @RequestParam(required = false) String estado) {
        log.debug("Listando farmacias distrito={} nombre={} estado={}", distrito, nombre, estado);
        return farmaciaServicio.listar(distrito, nombre, estado);
    }
    //HU10
    @GetMapping("/farmacias/{id}")
    public FarmaciaDTO buscarPorId(@PathVariable Long id) {
        log.info("Buscando farmacia {}", id);
        return farmaciaServicio.buscarPorId(id);
    }
    //HU11
    @GetMapping("/farmacias/{id}/ubicacion")
    public UbicacionDTO obtenerUbicacion(@PathVariable Long id) {
        return farmaciaServicio.obtenerUbicacion(id);
    }
    //HU15
    @PutMapping("/farmacias/{id}")
    public FarmaciaDTO editar(@PathVariable Long id, @RequestBody FarmaciaDTO farmaciaDTO) {
        return farmaciaServicio.editar(id, farmaciaDTO);
    }
    //HU16
    @PutMapping("/farmacias/{id}/perfil-comercial")
    public FarmaciaDTO actualizarPerfilComercial(@PathVariable Long id, @RequestBody PerfilComercialDTO perfilComercialDTO) {
        return farmaciaServicio.actualizarPerfilComercial(id, perfilComercialDTO);
    }
    //HU17 (baja logica: pasa a INACTIVA)
    @DeleteMapping("/farmacias/{id}")
    public void eliminar(@PathVariable Long id) {
        farmaciaServicio.eliminar(id);
    }
    //HU43
    @GetMapping("/farmacias/{id}/ofertas")
    public List<OfertaDTO> listarOfertas(@PathVariable Long id) {
        return ofertaServicio.listarPorFarmacia(id);
    }
    //HU27
    @GetMapping("/farmacias/{id}/calificaciones")
    public ResumenCalificacionesDTO listarCalificaciones(@PathVariable Long id) {
        return calificacionServicio.listar(id);
    }
    //HU47
    @PostMapping("/farmacias/{id}/calificaciones")
    public CalificacionDTO registrarCalificacion(@PathVariable Long id, @RequestBody CalificacionDTO calificacionDTO) {
        return calificacionServicio.registrar(id, calificacionDTO);
    }
}
