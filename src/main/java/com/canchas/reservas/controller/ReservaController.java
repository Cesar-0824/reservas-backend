package com.canchas.reservas.controller;

import com.canchas.reservas.model.EstadoReserva;
import com.canchas.reservas.model.Reserva;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // Importar para formato de fecha
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody Reserva reserva) {
        try {
            Reserva creada = reservaService.crearReserva(reserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            // Mensajes específicos para bad requests
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Para cualquier otra excepción no controlada
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor al crear la reserva: " + e.getMessage()); // Añadir mensaje para depuración
        }
    }


    @GetMapping
    public List<Reserva> listar() {
        return reservaService.listarTodas();
    }
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoReserva estado) {
        try {
            Reserva reserva = reservaService.findById(id);
            if (reserva == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
            }
            reserva.setEstado(estado);
            reservaService.actualizarReserva(id, reserva);
            return ResponseEntity.ok("Estado de la reserva actualizado a: " + estado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el estado: " + e.getMessage());
        }
    }


    @GetMapping("/usuario/{id}")
    public List<Reserva> listarPorUsuario(@PathVariable Integer id) {
        System.out.println("Buscando reservas del usuario ID: " + id);
        Usuario u = new Usuario(id); // Asumiendo que el constructor Usuario(id) es suficiente para buscar
        return reservaService.listarPorUsuario(u);
    }

    // MEJORA CLAVE: Manejo de fechas en el PathVariable
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Reserva>> listarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<Reserva> reservas = reservaService.listarPorFecha(fecha);
        if (reservas.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content si no hay reservas para la fecha
        }
        return ResponseEntity.ok(reservas);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Reserva reservaActualizado) {
        try {
            Reserva reserva = reservaService.actualizarReserva(id, reservaActualizado);
            return ResponseEntity.ok(reserva);
        } catch (IllegalArgumentException e) {
            // Este catch podría ser más específico si la excepción indica que no se encontró el ID
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarReserva(@PathVariable Integer id) {
        // Devolver una respuesta HTTP adecuada tras la eliminación
        try {
            reservaService.cancelarReserva(id);
            return ResponseEntity.noContent().build(); // 204 No Content si la eliminación fue exitosa
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found si la reserva no existe
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}