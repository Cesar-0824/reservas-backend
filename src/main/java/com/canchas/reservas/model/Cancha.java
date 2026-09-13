package com.canchas.reservas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "canchas")
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    private String tipo; // deporte: fútbol, vóley, tenis, básquet, etc.

    @Column(name = "precio_hora")
    @JsonProperty("precio_hora")
    private Double precioHora;

    @Enumerated(EnumType.STRING)
    private EstadoCancha estado = EstadoCancha.activa;

    private String imagen;

    private String modalidad; // ej. "7 vs 7", "1 vs 1 / 2 vs 2"

    private String dimensiones; // ej. "40 x 20 m"

    @Column(name = "tipo_superficie")
    private String tipoSuperficie; // ej. "Césped sintético", "Piso deportivo"

    private String iluminacion; // ej. "Nocturna", "Natural", "Sin iluminación"

    @Column(columnDefinition = "TEXT")
    private String caracteristicas; // ej. "Césped sintético · Iluminación · Vestuarios"

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "motivo_estado")
    private String motivoEstado; // solo aplica si estado = mantenimiento

    @Column(name = "observacion_estado", columnDefinition = "TEXT")
    private String observacionEstado;

    // --- Getters y Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getPrecioHora() { return precioHora; }
    public void setPrecioHora(Double precioHora) { this.precioHora = precioHora; }

    public EstadoCancha getEstado() { return estado; }
    public void setEstado(EstadoCancha estado) { this.estado = estado; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getDimensiones() { return dimensiones; }
    public void setDimensiones(String dimensiones) { this.dimensiones = dimensiones; }

    public String getTipoSuperficie() { return tipoSuperficie; }
    public void setTipoSuperficie(String tipoSuperficie) { this.tipoSuperficie = tipoSuperficie; }

    public String getIluminacion() { return iluminacion; }
    public void setIluminacion(String iluminacion) { this.iluminacion = iluminacion; }

    public String getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(String caracteristicas) { this.caracteristicas = caracteristicas; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMotivoEstado() { return motivoEstado; }
    public void setMotivoEstado(String motivoEstado) { this.motivoEstado = motivoEstado; }

    public String getObservacionEstado() { return observacionEstado; }
    public void setObservacionEstado(String observacionEstado) { this.observacionEstado = observacionEstado; }
}