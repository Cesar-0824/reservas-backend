package com.canchas.reservas.DTO;

import com.canchas.reservas.model.Rol;

import java.time.LocalDateTime;

public class LoginResponse {
    private String token;
    private String email;
    private Integer userId;
    private String nombreUsuario;
    private Rol rol;
    private LocalDateTime fechaRegistro;

    public LoginResponse(String token, String email, Integer userId, String nombreUsuario, Rol rol, LocalDateTime fechaRegistro) {
        this.token = token;
        this.email = email;
        this.userId = userId;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public Integer getUserId() { return userId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public Rol getRol() { return rol; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
