package com.ecommerce.users.exception;

public class CuentaDesactivadaException extends RuntimeException {
    public CuentaDesactivadaException() {
        super("La cuenta está desactivada. Contacte a un administrador");
    }
}
