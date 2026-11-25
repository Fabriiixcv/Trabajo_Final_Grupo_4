package com.upc.comparasalud.excepciones;

import java.util.List;

public class ReglaNegocioException extends RuntimeException {
    private final List<String> errores;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
        this.errores = List.of();
    }

    public ReglaNegocioException(String mensaje, List<String> errores) {
        super(mensaje);
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}
