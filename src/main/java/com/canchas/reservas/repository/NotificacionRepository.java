package com.canchas.reservas.repository;

import com.canchas.reservas.model.Notificacion;
import com.canchas.reservas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuario(Usuario usuario);
    boolean existsByUsuarioIdAndMensaje(Integer idUsuario, String mensaje);
}
