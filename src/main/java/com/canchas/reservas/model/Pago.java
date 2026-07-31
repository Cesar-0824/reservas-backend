package com.canchas.reservas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    public enum EstadoPago {
        exitoso,
        fallido,
        pendiente
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    private String pasarela;

    private LocalDateTime fechaPago;
    private Double monto;

    @ManyToOne
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    public Pago() {
    }

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
    }

    // Getters y setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getPasarela() {
        return pasarela;
    }

    public void setPasarela(String pasarela) {
        this.pasarela = pasarela;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }
    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
}
