package com.canchas.reservas.controller;

import com.canchas.reservas.model.EstadoReserva;
import com.canchas.reservas.model.Pago;
import com.canchas.reservas.model.Reserva;
import com.canchas.reservas.service.EmailService;
import com.canchas.reservas.service.MercadoPagoService;
import com.canchas.reservas.service.PagoService;
import com.canchas.reservas.service.ReservaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PagoController {

    private final PagoService pagoService;
    private final ReservaService reservaService;
    private final MercadoPagoService mercadoPagoService;
    private final EmailService emailService;

    public PagoController(PagoService pagoService, ReservaService reservaService,
                          MercadoPagoService mercadoPagoService, EmailService emailService) {
        this.pagoService = pagoService;
        this.reservaService = reservaService;
        this.mercadoPagoService = mercadoPagoService;
        this.emailService = emailService;
    }

    public static class ComprobanteRequest {
        public String tipo;
        public String ruc;
        public String razonSocial;
        public String direccionFiscal;
    }

    // NUEVO: listado de pagos para el panel administrativo
    @GetMapping("/pagos")
    public ResponseEntity<List<Pago>> listarPagos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    // Confirmar pago ficticio (NO ES NECESARIO para producción, solo para pruebas)
    @PostMapping("/reservas/{id}/pagar")
    public ResponseEntity<?> pagarReserva(@PathVariable Integer id, @RequestBody(required = false) ComprobanteRequest comprobante) {
        Reserva reserva = reservaService.findById(id);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
        }

        if (comprobante != null) {
            reserva.setTipoComprobante(comprobante.tipo);
            reserva.setRucComprobante(comprobante.ruc);
            reserva.setRazonSocialComprobante(comprobante.razonSocial);
            reserva.setDireccionFiscalComprobante(comprobante.direccionFiscal);
            reservaService.guardarReserva(reserva);
        }

        try {
            Reserva pagada = reservaService.confirmarPago(id);

            // Registrar también el pago ficticio en la tabla `pagos`,
            // para que el panel admin lo vea igual que un pago real.
            if (!pagoService.existePagoParaReserva(id)) {
                Pago pagoFicticio = new Pago();
                pagoFicticio.setReserva(pagada);
                pagoFicticio.setEstado(Pago.EstadoPago.exitoso);
                pagoFicticio.setMonto(pagada.getMontoTotal());
                pagoFicticio.setFechaPago(LocalDateTime.now());
                pagoFicticio.setPasarela("Ficticio (prueba)");
                pagoService.guardarPago(pagoFicticio);
            }

            return ResponseEntity.ok(pagada);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Crear preferencia de pago y devolver link (NECESARIO para crear pago real con MercadoPago)
    @PostMapping("/crear-preferencia")
    public ResponseEntity<?> crearPreferencia(@RequestParam Integer reservaId) {
        Reserva reserva = reservaService.findById(reservaId);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
        }

        // TODO: en local esto debe apuntar a tu frontend real (ej. http://localhost:3001/pago),
        // no a un dominio que no existe. En producción, cámbialo por tu dominio definitivo.
        String backUrl = "https://tusitio.com/pago?reservaId=" + reserva.getId();

        try {
            String linkPago = mercadoPagoService.crearPreferencia(
                    "Reserva Cancha #" + reserva.getId(),
                    1,
                    reserva.getMontoTotal(),
                    backUrl,
                    reserva.getId()
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
            String topic = (String) payload.get("topic");
            String id = (String) payload.get("id");

            if ("payment".equalsIgnoreCase(topic) && id != null) {
                Pago pago = mercadoPagoService.obtenerPagoPorId(id);

                if (pago != null && pago.getReserva() != null) {
                    // El objeto que llega de MercadoPagoService trae solo el ID de la reserva;
                    // hay que cargar la entidad real antes de asociarla y guardar.
                    Reserva reservaCompleta = reservaService.findById(pago.getReserva().getId());

                    if (reservaCompleta != null) {
                        pago.setReserva(reservaCompleta);
                        pago.setFechaPago(LocalDateTime.now());
                        pago.setPasarela("MercadoPago");

                        // Evita duplicar el registro si el webhook llega más de una vez
                        // (MercadoPago puede reintentar notificaciones).
                        if (!pagoService.existePagoParaReserva(reservaCompleta.getId())) {
                            pagoService.guardarPago(pago);
                        }

                        if (pago.getEstado() == Pago.EstadoPago.exitoso) {
                            try {
                                reservaService.confirmarPago(reservaCompleta.getId());
                            } catch (IllegalStateException e) {
                                System.err.println("No se pudo confirmar pago: " + e.getMessage());
                            }
                        }
                    }
                }
                return ResponseEntity.ok("OK");
            }

            // Para pagos ficticios enviados desde frontend (solo para pruebas, NO necesario en producción)
            if (payload.containsKey("reserva") && payload.get("estado") != null) {
                Map<String, Object> reservaMap = (Map<String, Object>) payload.get("reserva");
                Integer reservaId = (Integer) reservaMap.get("id");
                String estado = (String) payload.get("estado");

                if ("exitoso".equalsIgnoreCase(estado)) {
                    try {
                        Reserva reserva = reservaService.confirmarPago(reservaId);

                        if (!pagoService.existePagoParaReserva(reservaId)) {
                            Pago pagoFicticio = new Pago();
                            pagoFicticio.setReserva(reserva);
                            pagoFicticio.setEstado(Pago.EstadoPago.exitoso);
                            pagoFicticio.setMonto(reserva.getMontoTotal());
                            pagoFicticio.setFechaPago(LocalDateTime.now());
                            pagoFicticio.setPasarela("Ficticio (prueba)");
                            pagoService.guardarPago(pagoFicticio);
                        }

                        return ResponseEntity.ok("Pago ficticio confirmado y reserva actualizada");
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
                    } catch (IllegalStateException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
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