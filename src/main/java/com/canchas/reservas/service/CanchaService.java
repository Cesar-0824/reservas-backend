package com.canchas.reservas.service;

import com.canchas.reservas.model.Cancha;
import com.canchas.reservas.model.EstadoCancha;
import com.canchas.reservas.repository.CanchaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CanchaService {

    @Autowired
    private CanchaRepository canchaRepository;

    /**
     * Registra una nueva cancha.
     */
    public Cancha registrarCancha(String nombre, String tipo, Double precioHora, String imagenUrl,
                                  String modalidad, String dimensiones, String tipoSuperficie,
                                  String iluminacion, String caracteristicas, String descripcion) {
        Cancha cancha = new Cancha();
        cancha.setNombre(nombre);
        cancha.setTipo(tipo);
        cancha.setPrecioHora(precioHora);
        cancha.setEstado(EstadoCancha.activa); // Por defecto activa
        cancha.setImagen(imagenUrl);
        cancha.setModalidad(modalidad);
        cancha.setDimensiones(dimensiones);
        cancha.setTipoSuperficie(tipoSuperficie);
        cancha.setIluminacion(iluminacion);
        cancha.setCaracteristicas(caracteristicas);
        cancha.setDescripcion(descripcion);
        return canchaRepository.save(cancha);
    }

    /**
     * Lista todas las canchas registradas.
     */
    public List<Cancha> listarCanchas() {
        return canchaRepository.findAll();
    }

    /**
     * Actualiza los datos de una cancha por ID.
     */
    public Cancha actualizarCancha(Integer id, String nombre, String tipo, Double precioHora, String imagenUrl,
                                   String modalidad, String dimensiones, String tipoSuperficie,
                                   String iluminacion, String caracteristicas, String descripcion) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cancha no encontrada con ID: " + id));

        cancha.setNombre(nombre);
        cancha.setTipo(tipo);
        cancha.setPrecioHora(precioHora);
        cancha.setModalidad(modalidad);
        cancha.setDimensiones(dimensiones);
        cancha.setTipoSuperficie(tipoSuperficie);
        cancha.setIluminacion(iluminacion);
        cancha.setCaracteristicas(caracteristicas);
        cancha.setDescripcion(descripcion);

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            cancha.setImagen(imagenUrl);
        }

        return canchaRepository.save(cancha);
    }

    public Cancha cambiarEstado(Integer id, EstadoCancha nuevoEstado, String motivo, String observacion) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cancha no encontrada con ID: " + id));

        cancha.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoCancha.mantenimiento) {
            cancha.setMotivoEstado(motivo);
            cancha.setObservacionEstado(observacion);
        } else {
            cancha.setMotivoEstado(null);
            cancha.setObservacionEstado(null);
        }

        return canchaRepository.save(cancha);
    }

    /**
     * Elimina una cancha por ID.
     */
    public void eliminarCancha(Integer id) {
        if (!canchaRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Cancha no existe con ID: " + id);
        }
        canchaRepository.deleteById(id);
    }

}
