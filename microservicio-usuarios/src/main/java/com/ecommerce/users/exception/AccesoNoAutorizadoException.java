package com.ecommerce.users.exception;

public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException() {
        super("No tienes permiso para consultar o modificar la información de otro usuario");
    }
}
