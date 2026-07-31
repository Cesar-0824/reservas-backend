package com.canchas.reservas.controller;

import com.canchas.reservas.model.EstadoReserva;
import com.canchas.reservas.model.Pago;
import com.canchas.reservas.model.Reserva;
import com.canchas.reservas.service.MercadoPagoService;
import com.canchas.reservas.service.PagoService;
import com.canchas.reservas.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PagoController {

    private final PagoService pagoService;
    private final ReservaService reservaService;
    private final MercadoPagoService mercadoPagoService;

    public PagoController(PagoService pagoService, ReservaService reservaService, MercadoPagoService mercadoPagoService) {
        this.pagoService = pagoService;
        this.reservaService = reservaService;
        this.mercadoPagoService = mercadoPagoService;
    }

    // Confirmar pago ficticio (NO ES NECESARIO para producción, solo para pruebas)
    @PostMapping("/reservas/{id}/pagar")
    public ResponseEntity<?> pagarReserva(@PathVariable Integer id) {
        Reserva reserva = reservaService.findById(id);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
        }
        if (!reserva.getEstado().equals(EstadoReserva.confirmada)) {
            return ResponseEntity.badRequest().body("La reserva no está confirmada y no se puede pagar");
        }
        reservaService.actualizarEstadoReserva(id, EstadoReserva.pagada);
        return ResponseEntity.ok("Pago ficticio confirmado y reserva actualizada");
    }

    // Crear preferencia de pago y devolver link (NECESARIO para crear pago real con MercadoPago)
    @PostMapping("/crear-preferencia")
    public ResponseEntity<?> crearPreferencia(@RequestParam Integer reservaId) {
        Reserva reserva = reservaService.findById(reservaId);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
        }
        String backUrl = "https://tusitio.com/pago?reservaId=" + reserva.getId();

        // URL de retorno después del pago. Cambia según tu frontend.
        //String backUrl = "https://reservas.canchas.com.pe/pago";

        try {
            String linkPago = mercadoPagoService.crearPreferencia(
                    "Reserva Cancha #" + reserva.getId(),
                    1,
                    reserva.getMontoTotal(),
                    backUrl,
                    reserva.getId()  // Enviamos reservaId en external_reference
            );

            return ResponseEntity.ok(linkPago);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear preferencia de pago: " + e.getMessage());
        }
    }

    // Webhook para recibir notificaciones de MercadoPago (NECESARIO para actualizar estado pago automático)
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirNotificacion(@RequestBody Map<String, Object> payload) {
        try {
            // Para pagos reales vía MercadoPago
            String topic = (String) payload.get("topic");
            String id = (String) payload.get("id");

            if ("payment".equalsIgnoreCase(topic) && id != null) {
                Pago pago = mercadoPagoService.obtenerPagoPorId(id);

                if (pago != null) {
                    if (pago.getEstado() == Pago.EstadoPago.exitoso) {
                        Reserva reserva = reservaService.findById(pago.getReserva().getId());
                        if (reserva != null) {
                            reserva.setEstado(EstadoReserva.pagada);
                            reservaService.guardarReserva(reserva);
                        }
                    }
                    pagoService.guardarPago(pago);
                }
                return ResponseEntity.ok("OK");
            }

            // Para pagos ficticios enviados desde frontend (solo para pruebas, NO necesario en producción)
            if (payload.containsKey("reserva") && payload.get("estado") != null) {
                Map<String, Object> reservaMap = (Map<String, Object>) payload.get("reserva");
                Integer reservaId = (Integer) reservaMap.get("id");
                String estado = (String) payload.get("estado");

                if ("exitoso".equalsIgnoreCase(estado)) {
                    Reserva reserva = reservaService.findById(reservaId);
                    if (reserva != null) {
                        reserva.setEstado(EstadoReserva.pagada);
                        reservaService.guardarReserva(reserva);
                        return ResponseEntity.ok("Pago ficticio confirmado y reserva actualizada");
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
                    }
                }
            }

            return ResponseEntity.badRequest().body("Payload no válido");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error procesando webhook");
        }
    }

}
