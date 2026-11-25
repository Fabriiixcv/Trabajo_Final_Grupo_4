package com.upc.comparasalud.controladores;

import com.upc.comparasalud.dto.CategoriaDTO;
import com.upc.comparasalud.servicios.CategoriaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoriaController {
    @Autowired
    private CategoriaServicio categoriaServicio;

    //HU38
    @PostMapping("/categorias")
    public CategoriaDTO insertar(@RequestBody CategoriaDTO categoriaDTO) {
        return categoriaServicio.insertar(categoriaDTO);
    }
    //HU39
    @GetMapping("/categorias")
    public List<CategoriaDTO> listar() {
        return categoriaServicio.listar();
    }
    //HU40
    @PutMapping("/categorias/{id}")
    public CategoriaDTO editar(@PathVariable Long id, @RequestBody CategoriaDTO categoriaDTO) {
        return categoriaServicio.editar(id, categoriaDTO);
    }
    //HU41
    @DeleteMapping("/categorias/{id}")
    public void eliminar(@PathVariable Long id) {
        categoriaServicio.eliminar(id);
    }
}
