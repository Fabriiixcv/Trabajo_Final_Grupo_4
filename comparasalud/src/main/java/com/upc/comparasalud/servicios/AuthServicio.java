package com.upc.comparasalud.servicios;

import com.upc.comparasalud.dto.LoginDTO;
import com.upc.comparasalud.dto.SesionDTO;
import com.upc.comparasalud.entidades.Administrador;
import com.upc.comparasalud.entidades.Cliente;
import com.upc.comparasalud.entidades.Farmacia;
import com.upc.comparasalud.excepciones.AccesoDenegadoException;
import com.upc.comparasalud.excepciones.CredencialesIncorrectasException;
import com.upc.comparasalud.repositorios.AdministradorRepositorio;
import com.upc.comparasalud.repositorios.ClienteRepositorio;
import com.upc.comparasalud.repositorios.FarmaciaRepositorio;
import com.upc.comparasalud.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//Login de los tres roles: valida las credenciales y devuelve la sesion con su token JWT.
//Las contrasenas se guardan con BCrypt. Las cuentas antiguas (texto plano) siguen entrando y se
//convierten a BCrypt en su primer login.
@Service
public class AuthServicio {
    private static final String MENSAJE = "Credenciales incorrectas";

    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private FarmaciaRepositorio farmaciaRepositorio;
    @Autowired
    private AdministradorRepositorio administradorRepositorio;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private boolean esHash(String guardada) {
        return guardada != null && guardada.startsWith("$2");
    }

    private boolean coincide(String ingresada, String guardada) {
        if (guardada == null) return false;
        return esHash(guardada) ? passwordEncoder.matches(ingresada, guardada) : guardada.equals(ingresada);
    }

    private void exigirCredenciales(LoginDTO dto) {
        if (dto == null || dto.getCorreo() == null || dto.getContrasena() == null)
            throw new CredencialesIncorrectasException(MENSAJE);
    }

    //HU02
    public SesionDTO loginCliente(LoginDTO dto) {
        exigirCredenciales(dto);
        Cliente cliente = clienteRepositorio.findByCorreo(dto.getCorreo())
                .filter(c -> coincide(dto.getContrasena(), c.getContrasena()))
                .orElseThrow(() -> new CredencialesIncorrectasException(MENSAJE));
        if (!esHash(cliente.getContrasena())) {
            cliente.setContrasena(passwordEncoder.encode(dto.getContrasena()));
            clienteRepositorio.save(cliente);
        }
        SesionDTO sesion = new SesionDTO();
        sesion.setRol("CLIENTE");
        sesion.setIdCliente(cliente.getId());
        sesion.setNombre(cliente.getNombre());
        sesion.setToken(jwtUtil.generarToken("CLIENTE", cliente.getId(), cliente.getCorreo(), cliente.getNombre()));
        return sesion;
    }

    //HU53: una farmacia dada de baja no puede ingresar
    public SesionDTO loginFarmacia(LoginDTO dto) {
        exigirCredenciales(dto);
        Farmacia farmacia = farmaciaRepositorio.findByCorreo(dto.getCorreo())
                .filter(f -> coincide(dto.getContrasena(), f.getContrasena()))
                .orElseThrow(() -> new CredencialesIncorrectasException(MENSAJE));
        if (!"ACTIVA".equals(farmacia.getEstado()))
            throw new AccesoDenegadoException("La farmacia esta inactiva");
        if (!esHash(farmacia.getContrasena())) {
            farmacia.setContrasena(passwordEncoder.encode(dto.getContrasena()));
            farmaciaRepositorio.save(farmacia);
        }
        SesionDTO sesion = new SesionDTO();
        sesion.setRol("FARMACIA");
        sesion.setIdFarmacia(farmacia.getId());
        sesion.setNombre(farmacia.getNombre());
        sesion.setToken(jwtUtil.generarToken("FARMACIA", farmacia.getId(), farmacia.getCorreo(), farmacia.getNombre()));
        return sesion;
    }

    //HU52
    public SesionDTO loginAdministrador(LoginDTO dto) {
        exigirCredenciales(dto);
        Administrador administrador = administradorRepositorio.findByCorreo(dto.getCorreo())
                .filter(a -> coincide(dto.getContrasena(), a.getContrasena()))
                .orElseThrow(() -> new CredencialesIncorrectasException(MENSAJE));
        if (!esHash(administrador.getContrasena())) {
            administrador.setContrasena(passwordEncoder.encode(dto.getContrasena()));
            administradorRepositorio.save(administrador);
        }
        SesionDTO sesion = new SesionDTO();
        sesion.setRol("ADMINISTRADOR");
        sesion.setIdAdministrador(administrador.getId());
        sesion.setNombre(administrador.getNombre());
        sesion.setToken(jwtUtil.generarToken("ADMINISTRADOR", administrador.getId(), administrador.getCorreo(), administrador.getNombre()));
        return sesion;
    }
}
