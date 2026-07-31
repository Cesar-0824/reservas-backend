package com.canchas.reservas.service;

import com.canchas.reservas.DTO.NotificacionDTO;
import com.canchas.reservas.model.Notificacion;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    // Enviar una notificación a un usuario
    public Notificacion enviar(NotificacionDTO dto) {
        // Verifica si ya existe una notificación igual para el mismo usuario
        boolean yaExiste = notificacionRepository.existsByUsuarioIdAndMensaje(dto.getIdUsuario(), dto.getMensaje());

        if (yaExiste) {
            throw new RuntimeException("Ya se envió esta notificación anteriormente.");
        }

        // Crear y guardar la notificación
        Notificacion noti = new Notificacion();
        noti.setUsuario(new Usuario(dto.getIdUsuario())); // Solo se establece ID para relacionar
        noti.setMensaje(dto.getMensaje());

        return notificacionRepository.save(noti);
    }

    // Listar todas las notificaciones de un usuario
    public List<Notificacion> listarPorUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario inválido.");
        }
        return notificacionRepository.findByUsuario(usuario);
    }

    // Eliminar una notificación por su ID
    public void eliminar(Integer id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("Notificación no encontrada con ID: " + id);
        }
        notificacionRepository.deleteById(id);
    }
}
