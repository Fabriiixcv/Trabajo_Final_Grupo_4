package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.CategoriaDTO;
import com.upc.comparasalud.entidades.Categoria;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.CategoriaRepositorio;
import com.upc.comparasalud.repositorios.MedicamentoRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.upc.comparasalud.util.Validaciones.vacio;

@Service
public class CategoriaServicio {
    @Autowired
    private CategoriaRepositorio categoriaRepositorio;
    @Autowired
    private MedicamentoRepositorio medicamentoRepositorio;
    @Autowired
    private ModelMapper modelMapper;

    private Categoria buscar(Long id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoria con el id: " + id));
    }

    //HU38
    @Transactional
    public CategoriaDTO insertar(CategoriaDTO dto) {
        if (vacio(dto.getNombre()))
            throw new ReglaNegocioException("El nombre de la categoria es obligatorio");
        if (categoriaRepositorio.existsByNombreIgnoreCase(dto.getNombre()))
            throw new ReglaNegocioException("Ya existe la categoria: " + dto.getNombre());
        Categoria categoria = modelMapper.map(dto, Categoria.class);
        categoria.setId(null);
        return modelMapper.map(categoriaRepositorio.save(categoria), CategoriaDTO.class);
    }

    //HU39
    public List<CategoriaDTO> listar() {
        return categoriaRepositorio.findAll().stream()
                .sorted(java.util.Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(categoria -> modelMapper.map(categoria, CategoriaDTO.class))
                .collect(Collectors.toList());
    }

    //HU40
    @Transactional
    public CategoriaDTO editar(Long id, CategoriaDTO dto) {
        Categoria categoria = buscar(id);
        if (vacio(dto.getNombre()))
            throw new ReglaNegocioException("El nombre de la categoria es obligatorio");
        if (categoriaRepositorio.existsByNombreIgnoreCaseAndIdNot(dto.getNombre(), id))
            throw new ReglaNegocioException("Ya existe la categoria: " + dto.getNombre());
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return modelMapper.map(categoriaRepositorio.save(categoria), CategoriaDTO.class);
    }

    //HU41
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        if (medicamentoRepositorio.existsByCategoria_Id(id))
            throw new ReglaNegocioException("No se puede eliminar la categoria porque tiene medicamentos asociados");
        categoriaRepositorio.deleteById(id);
    }
}
