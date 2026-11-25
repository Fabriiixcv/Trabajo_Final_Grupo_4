package com.upc.comparasalud.excepciones;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //404: el recurso no existe
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return cuerpo(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    //400: validacion o regla de negocio (con lista "errores" cuando aplica)
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> reglaNegocio(ReglaNegocioException e) {
        return cuerpo(HttpStatus.BAD_REQUEST, e.getMessage(), e.getErrores());
    }

    //401: credenciales incorrectas en los login
    @ExceptionHandler(CredencialesIncorrectasException.class)
    public ResponseEntity<Map<String, Object>> credenciales(CredencialesIncorrectasException e) {
        return cuerpo(HttpStatus.UNAUTHORIZED, e.getMessage(), List.of());
    }

    //403: sin permiso (farmacia inactiva, medicamento de otra farmacia)
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccesoDenegadoException e) {
        return cuerpo(HttpStatus.FORBIDDEN, e.getMessage(), List.of());
    }

    //409: favorito repetido
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> conflicto(ConflictoException e) {
        return cuerpo(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException e) {
        return cuerpo(HttpStatus.CONFLICT, "La operacion viola una restriccion de la base de datos", List.of());
    }

    //400: cuerpo o parametros mal formados
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoInvalido(HttpMessageNotReadableException e) {
        return cuerpo(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es valido", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return cuerpo(HttpStatus.BAD_REQUEST, "El valor del parametro " + e.getName() + " no es valido", List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroFaltante(MissingServletRequestParameterException e) {
        return cuerpo(HttpStatus.BAD_REQUEST, "Falta el parametro: " + e.getParameterName(), List.of());
    }

    private ResponseEntity<Map<String, Object>> cuerpo(HttpStatus status, String mensaje, List<String> errores) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fecha", LocalDateTime.now());
        body.put("estado", status.value());
        body.put("mensaje", mensaje);
        if (!errores.isEmpty()) {
            body.put("errores", errores);
        }
        return ResponseEntity.status(status).body(body);
    }
}
