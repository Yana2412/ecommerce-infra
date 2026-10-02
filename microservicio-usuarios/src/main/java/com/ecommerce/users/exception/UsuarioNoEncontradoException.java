package com.ecommerce.users.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(Long id) {
        super("No existe un usuario con id: " + id);
    }
}
