package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.ClienteDTO;
import com.upc.comparasalud.dto.NombreClienteDTO;
import com.upc.comparasalud.entidades.Cliente;
import com.upc.comparasalud.excepciones.RecursoNoEncontradoException;
import com.upc.comparasalud.excepciones.ReglaNegocioException;
import com.upc.comparasalud.repositorios.CalificacionRepositorio;
import com.upc.comparasalud.repositorios.ClienteRepositorio;
import com.upc.comparasalud.repositorios.FarmaciaFavoritaRepositorio;
import com.upc.comparasalud.repositorios.FavoritoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.upc.comparasalud.util.Validaciones.correoValido;
import static com.upc.comparasalud.util.Validaciones.vacio;

@Service
public class ClienteServicio {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private FavoritoRepositorio favoritoRepositorio;
    @Autowired
    private FarmaciaFavoritaRepositorio farmaciaFavoritaRepositorio;
    @Autowired
    private CalificacionRepositorio calificacionRepositorio;

    //nunca se devuelve la contrasena
    private ClienteDTO aDTO(Cliente c) {
        return new ClienteDTO(c.getId(), c.getNombre(), c.getApellidos(), c.getCorreo(), null);
    }

    private Cliente buscar(Long id) {
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con el id: " + id));
    }

    //HU01 + HU08 (validacion del registro)
    @Transactional
    public ClienteDTO registrar(ClienteDTO dto) {
        List<String> errores = new ArrayList<>();
        if (vacio(dto.getNombre())) errores.add("El nombre es obligatorio");
        if (vacio(dto.getApellidos())) errores.add("Los apellidos son obligatorios");
        if (vacio(dto.getCorreo())) errores.add("El correo es obligatorio");
        else if (!correoValido(dto.getCorreo())) errores.add("El correo no tiene un formato valido");
        if (vacio(dto.getContrasena())) errores.add("La contrasena es obligatoria");
        else if (dto.getContrasena().length() < 6) errores.add("La contrasena debe tener al menos 6 caracteres");
        if (!errores.isEmpty())
            throw new ReglaNegocioException("Los datos de registro no son validos", errores);
        if (clienteRepositorio.existsByCorreo(dto.getCorreo()))
            throw new ReglaNegocioException("Ya existe un cliente con el correo: " + dto.getCorreo());
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellidos(dto.getApellidos());
        cliente.setCorreo(dto.getCorreo());
        cliente.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        return aDTO(clienteRepositorio.save(cliente));
    }

    //HU04
    public ClienteDTO buscarPorId(Long id) {
        return aDTO(buscar(id));
    }

    //HU37 (panel del administrador): todos los clientes, sin contrasena
    public List<ClienteDTO> listar() {
        List<ClienteDTO> lista = new ArrayList<>();
        for (Cliente c : clienteRepositorio.findAll()) lista.add(aDTO(c));
        lista.sort(java.util.Comparator.comparing(ClienteDTO::getApellidos, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ClienteDTO::getNombre, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    //HU44: solo nombre y apellidos
    @Transactional
    public ClienteDTO editarNombre(Long id, NombreClienteDTO dto) {
        Cliente cliente = buscar(id);
        if (vacio(dto.getNombre()))
            throw new ReglaNegocioException("El nombre es obligatorio");
        if (vacio(dto.getApellidos()))
            throw new ReglaNegocioException("Los apellidos son obligatorios");
        cliente.setNombre(dto.getNombre());
        cliente.setApellidos(dto.getApellidos());
        return aDTO(clienteRepositorio.save(cliente));
    }

    //HU05: elimina favoritos (medicamentos y farmacias), calificaciones y el cliente
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        favoritoRepositorio.deleteByCliente_Id(id);
        farmaciaFavoritaRepositorio.deleteByCliente_Id(id);
        calificacionRepositorio.deleteByCliente_Id(id);
        clienteRepositorio.deleteById(id);
    }
}
