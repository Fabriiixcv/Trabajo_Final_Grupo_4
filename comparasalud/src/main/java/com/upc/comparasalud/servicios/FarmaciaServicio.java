package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.FarmaciaDTO;
import com.upc.comparasalud.dto.PerfilComercialDTO;
import com.upc.comparasalud.dto.UbicacionDTO;
import com.upc.comparasalud.entidades.Farmacia;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.FarmaciaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.upc.comparasalud.util.Validaciones.*;

@Service
public class FarmaciaServicio {
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final String ACTIVA = "ACTIVA";

    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;

    //nunca se devuelve la contrasena; el correo solo se incluye en las respuestas de la propia farmacia
    private FarmaciaDTO aDTO(Farmacia f, boolean incluirCorreo) {
        return new FarmaciaDTO(f.getId(), f.getNombre(), f.getLicencia(), incluirCorreo ? f.getCorreo() : null, null,
                f.getDireccion(), f.getDistrito(), f.getTelefono(), f.getHorario(), f.getEstado());
    }

    //una farmacia dada de baja se trata como inexistente
    private Farmacia buscarActiva(Long id) {
        return farmaciaRepositorio.findByIdAndEstado(id, ACTIVA)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la farmacia con el id: " + id));
    }

    //HU09 + HU08 (validacion del registro)
    @Transactional
    public FarmaciaDTO insertar(FarmaciaDTO dto) {
        List<String> errores = new ArrayList<>();
        if (vacio(dto.getNombre())) errores.add("El nombre es obligatorio");
        else if (dto.getNombre().length() > 150) errores.add("El nombre no puede superar 150 caracteres");
        if (vacio(dto.getLicencia())) errores.add("La licencia es obligatoria");
        if (vacio(dto.getCorreo())) errores.add("El correo es obligatorio");
        else if (!correoValido(dto.getCorreo())) errores.add("El correo no tiene un formato valido");
        if (vacio(dto.getContrasena())) errores.add("La contrasena es obligatoria");
        else if (dto.getContrasena().length() < 6) errores.add("La contrasena debe tener al menos 6 caracteres");
        if (vacio(dto.getDireccion())) errores.add("La direccion es obligatoria");
        if (vacio(dto.getDistrito())) errores.add("El distrito es obligatorio");
        if (vacio(dto.getTelefono())) errores.add("El telefono es obligatorio");
        else if (!telefonoValido(dto.getTelefono())) errores.add("El telefono no tiene un formato valido");
        if (vacio(dto.getHorario())) errores.add("El horario es obligatorio");
        if (!errores.isEmpty())
            throw new ReglaNegocioException("Los datos de registro no son validos", errores);
        if (farmaciaRepositorio.existsByCorreo(dto.getCorreo()))
            throw new ReglaNegocioException("Ya existe una farmacia con el correo: " + dto.getCorreo());
        if (farmaciaRepositorio.existsByLicencia(dto.getLicencia()))
            throw new ReglaNegocioException("Ya existe una farmacia con la licencia: " + dto.getLicencia());
        Farmacia farmacia = new Farmacia();
        farmacia.setNombre(dto.getNombre());
        farmacia.setLicencia(dto.getLicencia());
        farmacia.setCorreo(dto.getCorreo());
        farmacia.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        farmacia.setDireccion(dto.getDireccion());
        farmacia.setDistrito(dto.getDistrito());
        farmacia.setTelefono(dto.getTelefono());
        farmacia.setHorario(dto.getHorario());
        farmacia.setEstado(ACTIVA);
        return aDTO(farmaciaRepositorio.save(farmacia), true);
    }

    //HU10
    public FarmaciaDTO buscarPorId(Long id) {
        return aDTO(buscarActiva(id), false);
    }

    //HU11
    public UbicacionDTO obtenerUbicacion(Long id) {
        Farmacia f = buscarActiva(id);
        return new UbicacionDTO(f.getId(), f.getNombre(), f.getDireccion(), f.getDistrito());
    }

    //HU12 (distrito), HU13 (nombre) y HU14 (estado=activa): una sola ruta, siempre solo farmacias activas
    public List<FarmaciaDTO> listar(String distrito, String nombre, String estado) {
        if (estado != null && !estado.trim().equalsIgnoreCase("activa"))
            throw new ReglaNegocioException("El filtro de estado solo admite el valor activa");
        return farmaciaRepositorio.findByEstado(ACTIVA).stream()
                .filter(f -> vacio(distrito) || f.getDistrito().equalsIgnoreCase(distrito.trim()))
                .filter(f -> vacio(nombre) || f.getNombre().toLowerCase().contains(nombre.trim().toLowerCase()))
                .sorted(Comparator.comparing(Farmacia::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(f -> aDTO(f, false))
                .collect(Collectors.toList());
    }

    //HU34 (panel del administrador): farmacias activas con todos sus datos, incluido el correo
    public List<FarmaciaDTO> listarActivasParaAdministrador() {
        return farmaciaRepositorio.findByEstado(ACTIVA).stream()
                .sorted(Comparator.comparing(Farmacia::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(f -> aDTO(f, true))
                .collect(Collectors.toList());
    }

    //HU15: nombre, licencia, direccion y distrito
    @Transactional
    public FarmaciaDTO editar(Long id, FarmaciaDTO dto) {
        Farmacia farmacia = buscarActiva(id);
        List<String> errores = new ArrayList<>();
        if (vacio(dto.getNombre())) errores.add("El nombre es obligatorio");
        else if (dto.getNombre().length() > 150) errores.add("El nombre no puede superar 150 caracteres");
        if (vacio(dto.getLicencia())) errores.add("La licencia es obligatoria");
        if (vacio(dto.getDireccion())) errores.add("La direccion es obligatoria");
        if (vacio(dto.getDistrito())) errores.add("El distrito es obligatorio");
        if (!errores.isEmpty())
            throw new ReglaNegocioException("Los datos de la farmacia no son validos", errores);
        if (farmaciaRepositorio.existsByLicenciaAndIdNot(dto.getLicencia(), id))
            throw new ReglaNegocioException("Ya existe otra farmacia con la licencia: " + dto.getLicencia());
        farmacia.setNombre(dto.getNombre());
        farmacia.setLicencia(dto.getLicencia());
        farmacia.setDireccion(dto.getDireccion());
        farmacia.setDistrito(dto.getDistrito());
        return aDTO(farmaciaRepositorio.save(farmacia), true);
    }

    //HU16: solo telefono y horario; el campo que no se envia conserva su valor
    @Transactional
    public FarmaciaDTO actualizarPerfilComercial(Long id, PerfilComercialDTO dto) {
        Farmacia farmacia = buscarActiva(id);
        if (dto.getTelefono() != null && !telefonoValido(dto.getTelefono()))
            throw new ReglaNegocioException("El telefono no tiene un formato valido");
        if (dto.getHorario() != null && dto.getHorario().isBlank())
            throw new ReglaNegocioException("El horario no puede estar vacio");
        if (dto.getTelefono() != null) farmacia.setTelefono(dto.getTelefono());
        if (dto.getHorario() != null) farmacia.setHorario(dto.getHorario());
        return aDTO(farmaciaRepositorio.save(farmacia), true);
    }

    //HU17: baja logica, la farmacia pasa a INACTIVA y se conservan sus datos
    @Transactional
    public void eliminar(Long id) {
        Farmacia farmacia = buscarActiva(id);
        farmacia.setEstado("INACTIVA");
        farmaciaRepositorio.save(farmacia);
    }
}
