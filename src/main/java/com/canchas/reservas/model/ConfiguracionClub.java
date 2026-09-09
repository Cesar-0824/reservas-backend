package com.canchas.reservas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_club")
public class ConfiguracionClub {

    @Id
    private Integer id = 1;

    // General
    private String nombreClub;
    private String emailContacto;
    private String telefono;
    private String horaApertura;
    private String horaCierre;

    // Reglas de reservas
    private Double duracionMinima = 1.0;
    private Double duracionMaxima = 3.0;
    private Integer anticipacionMaximaDias = 30;
    private Boolean permitirCancelaciones = true;

    // Notificaciones
    private Boolean notifNuevasReservas = true;
    private Boolean notifPagosExitosos = true;
    private Boolean notifReservasCanceladas = true;

    // Getters y setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreClub() { return nombreClub; }
    public void setNombreClub(String nombreClub) { this.nombreClub = nombreClub; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getHoraApertura() { return horaApertura; }
    public void setHoraApertura(String horaApertura) { this.horaApertura = horaApertura; }

    public String getHoraCierre() { return horaCierre; }
    public void setHoraCierre(String horaCierre) { this.horaCierre = horaCierre; }

    public Double getDuracionMinima() { return duracionMinima; }
    public void setDuracionMinima(Double duracionMinima) { this.duracionMinima = duracionMinima; }

    public Double getDuracionMaxima() { return duracionMaxima; }
    public void setDuracionMaxima(Double duracionMaxima) { this.duracionMaxima = duracionMaxima; }

    public Integer getAnticipacionMaximaDias() { return anticipacionMaximaDias; }
    public void setAnticipacionMaximaDias(Integer anticipacionMaximaDias) { this.anticipacionMaximaDias = anticipacionMaximaDias; }

    public Boolean getPermitirCancelaciones() { return permitirCancelaciones; }
    public void setPermitirCancelaciones(Boolean permitirCancelaciones) { this.permitirCancelaciones = permitirCancelaciones; }

    public Boolean getNotifNuevasReservas() { return notifNuevasReservas; }
    public void setNotifNuevasReservas(Boolean notifNuevasReservas) { this.notifNuevasReservas = notifNuevasReservas; }

    public Boolean getNotifPagosExitosos() { return notifPagosExitosos; }
    public void setNotifPagosExitosos(Boolean notifPagosExitosos) { this.notifPagosExitosos = notifPagosExitosos; }

    public Boolean getNotifReservasCanceladas() { return notifReservasCanceladas; }
    public void setNotifReservasCanceladas(Boolean notifReservasCanceladas) { this.notifReservasCanceladas = notifReservasCanceladas; }
}