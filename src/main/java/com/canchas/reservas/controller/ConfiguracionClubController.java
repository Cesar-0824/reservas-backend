package com.canchas.reservas.controller;

import com.canchas.reservas.model.ConfiguracionClub;
import com.canchas.reservas.service.ConfiguracionClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "*")
public class ConfiguracionClubController {

    @Autowired
    private ConfiguracionClubService service;

    @GetMapping
    public ResponseEntity<?> obtener() {
        try {
            return ResponseEntity.ok(service.obtenerConfiguracion());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/general")
    public ResponseEntity<?> guardarGeneral(@RequestBody Map<String, String> body) {
        try {
            ConfiguracionClub config = service.guardarGeneral(
                    body.get("nombreClub"), body.get("emailContacto"),
                    body.get("telefono"), body.get("horaApertura"), body.get("horaCierre")
            );
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/reservas")
    public ResponseEntity<?> guardarReservas(@RequestBody Map<String, Object> body) {
        try {
            ConfiguracionClub config = service.guardarReservas(
                    Double.valueOf(body.get("duracionMinima").toString()),
                    Double.valueOf(body.get("duracionMaxima").toString()),
                    Integer.valueOf(body.get("anticipacionMaximaDias").toString()),
                    Boolean.valueOf(body.get("permitirCancelaciones").toString())
            );
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/notificaciones")
    public ResponseEntity<?> guardarNotificaciones(@RequestBody Map<String, Object> body) {
        try {
            ConfiguracionClub config = service.guardarNotificaciones(
                    Boolean.valueOf(body.get("nuevasReservas").toString()),
                    Boolean.valueOf(body.get("pagosExitosos").toString()),
                    Boolean.valueOf(body.get("reservasCanceladas").toString())
            );
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}