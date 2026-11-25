package com.upc.comparasalud.util;

import java.util.regex.Pattern;

public class Validaciones {
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    //solo digitos, de 7 a 9
    private static final Pattern TELEFONO = Pattern.compile("^\\d{7,9}$");

    public static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static boolean correoValido(String correo) {
        return correo != null && CORREO.matcher(correo).matches();
    }

    public static boolean telefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono).matches();
    }
}
