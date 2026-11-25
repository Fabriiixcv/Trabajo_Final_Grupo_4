package com.upc.comparasalud.controladores;

import com.upc.comparasalud.dto.*;
import com.upc.comparasalud.servicios.AdministradorServicio;
import com.upc.comparasalud.servicios.AuthServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//El administrador es un usuario fijo (se crea al iniciar la aplicacion), no se registra por la API.
@RestController
@RequestMapping("/api")
public class AdministradorController {
    @Autowired
    private AdministradorServicio administradorServicio;
    @Autowired
    private AuthServicio authServicio;

    //HU52
    @PostMapping("/administradores/login")
    public SesionDTO login(@RequestBody LoginDTO loginDTO) {
        return authServicio.loginAdministrador(loginDTO);
    }
    //HU34
    @GetMapping("/administradores/farmacias")
    public ListadoDTO<FarmaciaDTO> farmacias() {
        return administradorServicio.farmacias();
    }
    //HU36
    @GetMapping("/administradores/ofertas")
    public ListadoDTO<OfertaDTO> ofertas() {
        return administradorServicio.ofertas();
    }
    //HU37
    @GetMapping("/administradores/clientes")
    public ListadoDTO<ClienteDTO> clientes() {
        return administradorServicio.clientes();
    }
    //HU46
    @GetMapping("/administradores/categorias")
    public ListadoDTO<CategoriaDTO> categorias() {
        return administradorServicio.categorias();
    }
    //HU50
    @GetMapping("/administradores/medicamentos")
    public ListadoDTO<MedicamentoDTO> medicamentos() {
        return administradorServicio.medicamentos();
    }
    //HU56
    @DeleteMapping("/administradores/clientes/{id}")
    public void eliminarCliente(@PathVariable Long id) {
        administradorServicio.eliminarCliente(id);
    }
    //HU57
    @DeleteMapping("/administradores/farmacias/{id}")
    public void eliminarFarmacia(@PathVariable Long id) {
        administradorServicio.eliminarFarmacia(id);
    }
}
