package com.canchas.reservas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Table(name ="reservas")
@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario",nullable = false)
    @JsonIgnoreProperties("reservas")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_cancha", nullable = false)
    private Cancha cancha;



    @Column(name = "fecha_reserva")
    private LocalDate fechaReserva;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.pendiente;

    private String metodoPago;

    @Column(name = "comprobante_url")
    private String comprobanteUrl;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @Column(name = "observacion_cancelacion")
    private String observacionCancelacion;

    @Column(name = "cancelado_por")
    private String canceladoPor;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;


    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("reserva")
    private List<Pago> pagos = new ArrayList<>();

    @Column(name = "tipo_comprobante")
    private String tipoComprobante; // "boleta" | "factura"

    @Column(name = "ruc_comprobante")
    private String rucComprobante;

    @Column(name = "razon_social_comprobante")
    private String razonSocialComprobante;

    @Column(name = "direccion_fiscal_comprobante")
    private String direccionFiscalComprobante;

    @Column(name = "fecha_limite_pago")
    private LocalDateTime fechaLimitePago;


    @Column(name = "recordatorio_24h_enviado")
    private Boolean recordatorio24hEnviado = false;

    @Column(name = "recordatorio_2h_enviado")
    private Boolean recordatorio2hEnviado = false;

    @Column(name = "monto_total")
    private Double montoTotal;
    public List<Pago> getPagos() {
        return pagos;
    }

    public Boolean getRecordatorio24hEnviado() {
        return recordatorio24hEnviado;
    }

    public void setRecordatorio24hEnviado(Boolean recordatorio24hEnviado) {
        this.recordatorio24hEnviado = recordatorio24hEnviado;
    }

    public Boolean getRecordatorio2hEnviado() {
        return recordatorio2hEnviado;
    }

    public void setRecordatorio2hEnviado(Boolean recordatorio2hEnviado) {
        this.recordatorio2hEnviado = recordatorio2hEnviado;
    }
    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Cancha getCancha() {
        return cancha;
    }

    public void setCancha(Cancha cancha) {
        this.cancha = cancha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getObservacionCancelacion() {
        return observacionCancelacion;
    }

    public void setObservacionCancelacion(String observacionCancelacion) {
        this.observacionCancelacion = observacionCancelacion;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }


    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getRucComprobante() {
        return rucComprobante;
    }

    public void setRucComprobante(String rucComprobante) {
        this.rucComprobante = rucComprobante;
    }

    public String getRazonSocialComprobante() {
        return razonSocialComprobante;
    }

    public void setRazonSocialComprobante(String razonSocialComprobante) {
        this.razonSocialComprobante = razonSocialComprobante;
    }

    public String getDireccionFiscalComprobante() {
        return direccionFiscalComprobante;
    }

    public void setDireccionFiscalComprobante(String direccionFiscalComprobante) {
        this.direccionFiscalComprobante = direccionFiscalComprobante;
    }
    public LocalDateTime getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(LocalDateTime fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

}