package com.upc.comparasalud.excepciones;

public class CredencialesIncorrectasException extends RuntimeException {
    public CredencialesIncorrectasException(String mensaje) {
        super(mensaje);
    }
}
