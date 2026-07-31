package com.canchas.reservas.controller;

import com.canchas.reservas.DTO.NotificacionDTO;
import com.canchas.reservas.model.Notificacion;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private NotificacionService notiService;

    @PostMapping("/enviar")
    public ResponseEntity<?> enviar(@RequestBody NotificacionDTO dto) {
        try {
            Notificacion noti = notiService.enviar(dto);
            return ResponseEntity.ok(noti);
        } catch (Exception e) {
            return ResponseEntity.status(409).body("Notificación duplicada: " + e.getMessage());
        }
    }

    @GetMapping("/usuario/{id}")
    public List<Notificacion> listarPorUsuario(@PathVariable Integer id) {
        Usuario u = new Usuario(id);
        u.setId(id);
        return notiService.listarPorUsuario(u);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            notiService.eliminar(id);
            return ResponseEntity.ok("Notificación eliminada");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar notificación: " + e.getMessage());
        }
    }
}

