package com.canchas.reservas.service;

import com.canchas.reservas.DTO.NotificacionDTO;
import com.canchas.reservas.model.Notificacion;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.repository.NotificacionRepository;
import com.canchas.reservas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private NotificacionRepository notificacionRepository;

    // Enviar una notificación a un usuario
    public Notificacion enviar(NotificacionDTO dto) {
        boolean yaExiste = notificacionRepository.existsByUsuarioIdAndMensaje(dto.getIdUsuario(), dto.getMensaje());

        if (yaExiste) {
            throw new RuntimeException("Ya se envió esta notificación anteriormente.");
        }

        Notificacion noti = new Notificacion();
        noti.setUsuario(new Usuario(dto.getIdUsuario()));
        noti.setMensaje(dto.getMensaje());
        noti.setIdReserva(dto.getIdReserva()); // NUEVO

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
    public Notificacion marcarLeida(Integer id) {
        Notificacion noti = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        noti.setLeida(true);
        return notificacionRepository.save(noti);
    }
    public void marcarTodasLeidas(Usuario usuario) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioAndLeida(usuario, false);
        for (Notificacion n : noLeidas) {
            n.setLeida(true);
        }
        notificacionRepository.saveAll(noLeidas);
    }


    // Notifica a todos los usuarios con rol admin (una notificación por cada uno)
    public void notificarAdmins(String mensaje) {
        List<Usuario> admins = usuarioRepository.findAll().stream()
                .filter(u -> "admin".equalsIgnoreCase(String.valueOf(u.getRol())))
                .toList();

        for (Usuario admin : admins) {
            try {
                NotificacionDTO dto = new NotificacionDTO();
                dto.setIdUsuario(admin.getId());
                dto.setMensaje(mensaje);
                enviar(dto);
            } catch (Exception e) {
                System.err.println("No se pudo notificar al admin " + admin.getId() + ": " + e.getMessage());
            }
        }
    }

}
