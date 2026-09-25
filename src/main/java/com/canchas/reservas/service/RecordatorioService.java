package com.canchas.reservas.service;

import com.canchas.reservas.model.Reserva;
import com.canchas.reservas.model.EstadoReserva;
import com.canchas.reservas.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecordatorioService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificacionService notificacionService;

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Corre cada 15 minutos
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void revisarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Reserva> reservasActivas = reservaRepository.findByEstadoIn(
                List.of(EstadoReserva.confirmada, EstadoReserva.pagada)
        );

        for (Reserva r : reservasActivas) {
            if (r.getFechaReserva() == null || r.getHoraInicio() == null) continue;

            LocalDateTime fechaHoraReserva = LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio());
            long minutosParaReserva = java.time.Duration.between(ahora, fechaHoraReserva).toMinutes();

            // Recordatorio 24h (ventana: entre 23h45 y 24h15 antes, para no perderlo entre corridas)
            if (!Boolean.TRUE.equals(r.getRecordatorio24hEnviado())
                    && minutosParaReserva <= 24 * 60 + 15
                    && minutosParaReserva >= 24 * 60 - 15) {
                enviarRecordatorio(r, "24 horas");
                r.setRecordatorio24hEnviado(true);
                reservaRepository.save(r);
            }

            // Recordatorio 2h
            if (!Boolean.TRUE.equals(r.getRecordatorio2hEnviado())
                    && minutosParaReserva <= 2 * 60 + 15
                    && minutosParaReserva >= 2 * 60 - 15) {
                enviarRecordatorio(r, "2 horas");
                r.setRecordatorio2hEnviado(true);
                reservaRepository.save(r);
            }
        }
    }
    // ↓↓↓ NUEVO MÉTODO, va aquí ↓↓↓
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void marcarReservasVencidas() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Reserva> reservasActivas = reservaRepository.findByEstadoIn(
                List.of(EstadoReserva.pendiente, EstadoReserva.confirmada)
        );

        for (Reserva r : reservasActivas) {
            if (r.getFechaReserva() == null || r.getHoraInicio() == null) continue;

            LocalDateTime fechaHoraReserva = LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio());

            if (fechaHoraReserva.isBefore(ahora)) {
                r.setEstado(EstadoReserva.vencida);
                reservaRepository.save(r);
            }
        }
    }

    private void enviarRecordatorio(Reserva r, String horasRestantes) {
        String fechaFmt = r.getFechaReserva().format(FECHA_FMT);
        String cancha = r.getCancha() != null ? r.getCancha().getNombre() : "tu cancha";

        try {
            com.canchas.reservas.DTO.NotificacionDTO dto = new com.canchas.reservas.DTO.NotificacionDTO();
            dto.setIdUsuario(r.getUsuario().getId());
            dto.setMensaje("Tu reserva en " + cancha + " del " + fechaFmt +
                    " a las " + r.getHoraInicio() + " es en " + horasRestantes + ".");
            dto.setIdReserva(r.getId()); // NUEVO
            notificacionService.enviar(dto);
        } catch (Exception e) {
            System.err.println("No se pudo guardar notificación de recordatorio: " + e.getMessage());
        }

        try {
            emailService.enviarCorreoRecordatorio(
                    r.getUsuario().getEmail(),
                    r.getUsuario().getNombre(),
                    cancha,
                    fechaFmt,
                    String.valueOf(r.getHoraInicio()),
                    String.valueOf(r.getHoraFin()),
                    horasRestantes
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar correo de recordatorio: " + e.getMessage());
        }
    }
    @Scheduled(fixedRate = 60 * 1000)
    public void cancelarReservasPorVencimientoPago() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Reserva> pendientesPago = reservaRepository.findByEstadoIn(
                List.of(EstadoReserva.confirmada)
        );

        for (Reserva r : pendientesPago) {
            if (r.getFechaLimitePago() == null) continue;

            if (r.getFechaLimitePago().isBefore(ahora)) {
                r.setEstado(EstadoReserva.cancelada);
                r.setCanceladoPor("sistema");
                r.setMotivoCancelacion("Pago no realizado dentro del plazo establecido");
                reservaRepository.save(r);

                String cancha = r.getCancha() != null ? r.getCancha().getNombre() : "tu cancha";

                // Notificación interna al usuario
                try {
                    com.canchas.reservas.DTO.NotificacionDTO dto = new com.canchas.reservas.DTO.NotificacionDTO();
                    dto.setIdUsuario(r.getUsuario().getId());
                    dto.setMensaje("Tu reserva en " + cancha + " del " + r.getFechaReserva().format(FECHA_FMT) +
                            " fue cancelada automáticamente por falta de pago.");
                    dto.setIdReserva(r.getId()); // NUEVO
                    notificacionService.enviar(dto);
                } catch (Exception e) {
                    System.err.println("No se pudo notificar al usuario (vencimiento pago): " + e.getMessage());
                }

                // Notificación interna a los admins
                try {
                    notificacionService.notificarAdmins(
                            "Reserva #" + r.getId() + " de " + r.getUsuario().getNombre() +
                                    " fue cancelada automáticamente por falta de pago."
                    );
                } catch (Exception e) {
                    System.err.println("No se pudo notificar a los admins (vencimiento pago): " + e.getMessage());
                }

                // Correo al usuario (ya existente)
                try {
                    emailService.enviarCorreoCancelacion(
                            r.getUsuario().getEmail(),
                            r.getUsuario().getNombre(),
                            cancha,
                            r.getFechaReserva().format(FECHA_FMT),
                            String.valueOf(r.getHoraInicio()),
                            String.valueOf(r.getHoraFin()),
                            r.getMotivoCancelacion(),
                            true
                    );
                } catch (Exception e) {
                    System.err.println("No se pudo enviar correo de cancelación por vencimiento: " + e.getMessage());
                }

                // Correo institucional al admin (preparado, inactivo hasta configurar club.email.institucional)
                try {
                    emailService.enviarCorreoInstitucionalAdmin(
                            "Cancelación automática por falta de pago",
                            "<p>La reserva #" + r.getId() + " de <strong>" + r.getUsuario().getNombre() +
                                    "</strong> fue cancelada automáticamente por falta de pago.</p>"
                    );
                } catch (Exception e) {
                    System.err.println("No se pudo enviar correo institucional (vencimiento pago): " + e.getMessage());
                }
            }
        }
    }
}