package com.ecommerce.users.dto;

public class LoginResponse {

    private String token;
    private final String tipo = "Bearer";
    private String correo;
    private String rol;

    public LoginResponse(String token, String correo, String rol) {
        this.token = token;
        this.correo = correo;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }
}
