package com.upc.comparasalud.controladores;

import com.upc.comparasalud.dto.*;
import com.upc.comparasalud.servicios.AuthServicio;
import com.upc.comparasalud.servicios.ClienteServicio;
import com.upc.comparasalud.servicios.FavoritoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClienteController {
    @Autowired
    private ClienteServicio clienteServicio;
    @Autowired
    private FavoritoServicio favoritoServicio;
    @Autowired
    private AuthServicio authServicio;

    //HU01 (con las validaciones de HU08)
    @PostMapping("/clientes")
    public ClienteDTO registrar(@RequestBody ClienteDTO clienteDTO) {
        return clienteServicio.registrar(clienteDTO);
    }
    //HU02
    @PostMapping("/clientes/login")
    public SesionDTO login(@RequestBody LoginDTO loginDTO) {
        return authServicio.loginCliente(loginDTO);
    }
    //HU04
    @GetMapping("/clientes/{id}")
    public ClienteDTO buscarPorId(@PathVariable Long id) {
        return clienteServicio.buscarPorId(id);
    }
    //HU44
    @PutMapping("/clientes/{id}/nombre")
    public ClienteDTO editarNombre(@PathVariable Long id, @RequestBody NombreClienteDTO nombreDTO) {
        return clienteServicio.editarNombre(id, nombreDTO);
    }
    //HU05
    @DeleteMapping("/clientes/{id}")
    public void eliminar(@PathVariable Long id) {
        clienteServicio.eliminar(id);
    }

    //HU03: responde 201 con el id del medicamento
    @PostMapping("/clientes/{id}/medicamentos-favoritos")
    public ResponseEntity<IdDTO> agregarMedicamentoFavorito(@PathVariable Long id, @RequestBody FavoritoDTO favoritoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoServicio.agregarMedicamento(id, favoritoDTO));
    }
    //HU19
    @GetMapping("/clientes/{id}/medicamentos-favoritos")
    public List<MedicamentoPrecioDTO> listarMedicamentosFavoritos(@PathVariable Long id) {
        return favoritoServicio.listarMedicamentos(id);
    }
    //HU18: responde 204
    @DeleteMapping("/clientes/{id}/medicamentos-favoritos/{medicamentoId}")
    public ResponseEntity<Void> quitarMedicamentoFavorito(@PathVariable Long id, @PathVariable Long medicamentoId) {
        favoritoServicio.quitarMedicamento(id, medicamentoId);
        return ResponseEntity.noContent().build();
    }
    //HU28: responde 201 con el id de la farmacia
    @PostMapping("/clientes/{id}/farmacias-favoritas")
    public ResponseEntity<IdDTO> agregarFarmaciaFavorita(@PathVariable Long id, @RequestBody FavoritoDTO favoritoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoServicio.agregarFarmacia(id, favoritoDTO));
    }
    //HU20
    @GetMapping("/clientes/{id}/farmacias-favoritas")
    public List<UbicacionDTO> listarFarmaciasFavoritas(@PathVariable Long id) {
        return favoritoServicio.listarFarmacias(id);
    }
    //HU51: responde 204
    @DeleteMapping("/clientes/{id}/farmacias-favoritas/{farmaciaId}")
    public ResponseEntity<Void> quitarFarmaciaFavorita(@PathVariable Long id, @PathVariable Long farmaciaId) {
        favoritoServicio.quitarFarmacia(id, farmaciaId);
        return ResponseEntity.noContent().build();
    }
}
