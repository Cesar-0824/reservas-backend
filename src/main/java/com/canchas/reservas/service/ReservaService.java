package com.canchas.reservas.service;

import com.canchas.reservas.DTO.NotificacionDTO;
import com.canchas.reservas.model.*;
import com.canchas.reservas.repository.CanchaRepository;
import com.canchas.reservas.repository.ReservaRepository;
import com.canchas.reservas.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.canchas.reservas.model.EstadoCancha;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;



@Service
public class ReservaService {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CanchaRepository canchaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private NotificacionService notiService;

    @Autowired
    private ConfiguracionClubService configuracionClubService;

    public Reserva crearReserva(Reserva reserva) {
        if (reserva.getUsuario() == null ||
                reserva.getUsuario().getId() == null ||
                !usuarioRepository.existsById(reserva.getUsuario().getId())) {
            throw new IllegalArgumentException("Usuario inválido o no existe");
        }

        if (reserva.getCancha() == null || reserva.getCancha().getId() == null) {
            throw new IllegalArgumentException("Cancha inválida o no existe");
        }

        // Traemos la cancha completa (el frontend solo manda el id)
        Cancha cancha = canchaRepository.findById(reserva.getCancha().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cancha inválida o no existe"));
        reserva.setCancha(cancha);

        validarReglasDeReserva(reserva);
        validarSolapamiento(reserva);

        // Monto calculado SIEMPRE en el backend, nunca se confía en lo que mande el frontend
        long minutosDuracion = Duration.between(reserva.getHoraInicio(), reserva.getHoraFin()).toMinutes();
        double precioHora = cancha.getPrecioHora() != null ? cancha.getPrecioHora() : 0.0;
        reserva.setMontoTotal(precioHora * (minutosDuracion / 60.0));

        return reservaRepository.save(reserva);
    }

    // --- Evita que dos reservas se solapen en la misma cancha/fecha ---
    private void validarSolapamiento(Reserva reserva) {
        List<Reserva> existentes = reservaRepository.findByCanchaIdAndFechaReservaAndEstadoIn(
                reserva.getCancha().getId(),
                reserva.getFechaReserva(),
                List.of(EstadoReserva.pendiente, EstadoReserva.confirmada, EstadoReserva.pagada)
        );

        LocalTime nuevoInicio = reserva.getHoraInicio();
        LocalTime nuevoFin = reserva.getHoraFin();

        for (Reserva existente : existentes) {
            // Si es una edición de la misma reserva, se ignora a sí misma
            if (reserva.getId() != null && reserva.getId().equals(existente.getId())) continue;

            boolean solapa = nuevoInicio.isBefore(existente.getHoraFin())
                    && existente.getHoraInicio().isBefore(nuevoFin);

            if (solapa) {
                throw new IllegalArgumentException(
                        "Ese horario ya está ocupado (reserva existente de " +
                                existente.getHoraInicio() + " a " + existente.getHoraFin() + ")");
            }
        }
    }
    // --- Valida horario de atención, duración y anticipación máxima ---
    private void validarReglasDeReserva(Reserva reserva) {
        ConfiguracionClub config = configuracionClubService.obtenerConfiguracion();

        LocalDate fecha = reserva.getFechaReserva();
        LocalTime horaInicio = reserva.getHoraInicio();
        LocalTime horaFin = reserva.getHoraFin();

        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Fecha, hora de inicio y hora de fin son obligatorias");
        }
        LocalDateTime fechaHoraReserva = LocalDateTime.of(fecha, horaInicio);
        if (fechaHoraReserva.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede reservar en una fecha u hora que ya pasó");
        }
        if (reserva.getCancha() != null && reserva.getCancha().getEstado() != EstadoCancha.activa) {
            throw new IllegalArgumentException("La cancha no está disponible actualmente para reservas");
        }

        // --- Horario de atención ---
        // Si horaApertura y horaCierre son iguales (ej. 00:00 - 00:00), se interpreta
        // como "abierto 24 horas" y no se aplica ninguna restricción de horario.
        if (config.getHoraApertura() != null && config.getHoraCierre() != null
                && !config.getHoraApertura().equals(config.getHoraCierre())) {

            LocalTime apertura = LocalTime.parse(config.getHoraApertura());
            LocalTime cierre = LocalTime.parse(config.getHoraCierre());

            boolean cruzaMedianoche = cierre.isBefore(apertura);

            boolean horaInicioValida;
            boolean horaFinValida;

            if (cruzaMedianoche) {
                horaInicioValida = !horaInicio.isBefore(apertura) || !horaInicio.isAfter(cierre);
                horaFinValida = !horaFin.isBefore(apertura) || !horaFin.isAfter(cierre);
            } else {
                horaInicioValida = !horaInicio.isBefore(apertura) && !horaInicio.isAfter(cierre);
                horaFinValida = !horaFin.isBefore(apertura) && !horaFin.isAfter(cierre);
            }

            if (!horaInicioValida || !horaFinValida) {
                throw new IllegalArgumentException(
                        "La reserva debe estar dentro del horario de atención (" +
                                apertura + " - " + cierre + ")");
            }
        }

        // --- Duración mínima y máxima ---
        long minutosDuracion = Duration.between(horaInicio, horaFin).toMinutes();
        if (minutosDuracion <= 0) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (config.getDuracionMinima() != null && minutosDuracion < config.getDuracionMinima() * 60L) {
            throw new IllegalArgumentException(
                    "La duración mínima de una reserva es de " + config.getDuracionMinima() + " hora(s)");
        }
        if (config.getDuracionMaxima() != null && minutosDuracion > config.getDuracionMaxima() * 60L) {
            throw new IllegalArgumentException(
                    "La duración máxima de una reserva es de " + config.getDuracionMaxima() + " hora(s)");
        }

        // --- Anticipación máxima ---
        if (config.getAnticipacionMaximaDias() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), fecha);

            if (dias < 0) {
                throw new IllegalArgumentException("No se puede reservar en una fecha pasada");
            }
            if (dias > config.getAnticipacionMaximaDias()) {
                throw new IllegalArgumentException(
                        "No se puede reservar con más de " + config.getAnticipacionMaximaDias() + " días de anticipación");
            }
        }
    }

    public List<Reserva> listarPorUsuario(Usuario usuario) {
        return reservaRepository.findByUsuario(usuario);
    }

    public List<Reserva> listarPorFecha(LocalDate fecha) {
        return reservaRepository.findByFechaReserva(fecha).stream()
                .filter(r -> r.getEstado() != EstadoReserva.cancelada && r.getEstado() != EstadoReserva.vencida)
                .toList();
    }

    public Reserva actualizarEstadoReserva(Integer id, EstadoReserva nuevoEstado) {
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setEstado(nuevoEstado);
            return reservaRepository.save(reserva);
        }).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    // Usado por el ADMIN (cancelar con motivo, confirmar, etc.) — no se restringe por permitirCancelaciones
    public Reserva actualizarReserva(Integer id, Reserva reservaActualizado) {
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setFechaReserva(reservaActualizado.getFechaReserva());
            reserva.setHoraInicio(reservaActualizado.getHoraInicio());
            reserva.setHoraFin(reservaActualizado.getHoraFin());
            reserva.setEstado(reservaActualizado.getEstado());
            reserva.setMetodoPago(reservaActualizado.getMetodoPago());
            reserva.setComprobanteUrl(reservaActualizado.getComprobanteUrl());
            reserva.setMontoTotal(reservaActualizado.getMontoTotal());

            reserva.setMotivoCancelacion(reservaActualizado.getMotivoCancelacion());
            reserva.setObservacionCancelacion(reservaActualizado.getObservacionCancelacion());
            reserva.setCanceladoPor(reservaActualizado.getCanceladoPor());

            // 🔧 Limpieza: si la reserva queda cancelada, no debe quedar info de pago
            if (reserva.getEstado() == EstadoReserva.cancelada) {
                reserva.setMetodoPago(null);
                reserva.setComprobanteUrl(null);
                // monto_total lo dejo como referencia histórica de cuánto costaba,
                // pero si prefieres limpiarlo también: reserva.setMontoTotal(null);
            }

            Integer usuarioId = reservaActualizado.getUsuario().getId();
            Integer canchaId = reservaActualizado.getCancha().getId();

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            Cancha cancha = canchaRepository.findById(canchaId)
                    .orElseThrow(() -> new IllegalArgumentException("Cancha no encontrada"));

            reserva.setUsuario(usuario);
            reserva.setCancha(cancha);

            Reserva guardada = reservaRepository.save(reserva);

            if (guardada.getEstado() == EstadoReserva.cancelada
                    && guardada.getMotivoCancelacion() != null) {

                String canceladoPor = guardada.getCanceladoPor();
                boolean esAdmin = "admin".equalsIgnoreCase(canceladoPor);
                // El frontend actual envía "cliente"; se acepta también "usuario" por compatibilidad futura
                boolean esUsuario = "cliente".equalsIgnoreCase(canceladoPor) || "usuario".equalsIgnoreCase(canceladoPor);

                if (esAdmin) {
                    try {
                        NotificacionDTO dto = new NotificacionDTO();
                        dto.setIdUsuario(usuario.getId());
                        dto.setMensaje("Tu reserva del " + guardada.getFechaReserva() +
                                " fue cancelada. Motivo: " + guardada.getMotivoCancelacion());
                        dto.setIdReserva(guardada.getId()); // NUEVO
                        notiService.enviar(dto);
                    } catch (Exception e) {
                        System.err.println("No se pudo enviar notificación al usuario: " + e.getMessage());
                    }

                    try {
                        emailService.enviarCorreoCancelacion(
                                usuario.getEmail(),
                                usuario.getNombre(),
                                cancha.getNombre(),
                                guardada.getFechaReserva().toString(),
                                String.valueOf(guardada.getHoraInicio()),
                                String.valueOf(guardada.getHoraFin()),
                                guardada.getMotivoCancelacion(),
                                false
                        );
                    } catch (Exception e) {
                        System.err.println("No se pudo enviar correo de cancelación: " + e.getMessage());
                    }

                } else if (esUsuario) {
                    // Usuario cancela su propia reserva → notificar SOLO al admin (nunca al propio usuario)
                    try {
                        notiService.notificarAdmins(
                                usuario.getNombre() + " canceló su reserva del " + guardada.getFechaReserva() +
                                        " (" + guardada.getHoraInicio() + " - " + guardada.getHoraFin() + ")" +
                                        " en " + cancha.getNombre() + ". Motivo: " + guardada.getMotivoCancelacion()
                        );
                    } catch (Exception e) {
                        System.err.println("No se pudo notificar a los admins: " + e.getMessage());
                    }

                    try {
                        emailService.enviarCorreoInstitucionalAdmin(
                                "Cancelación de reserva - " + usuario.getNombre(),
                                "<p>El usuario <strong>" + usuario.getNombre() + "</strong> canceló su reserva del " +
                                        guardada.getFechaReserva() + " en " + cancha.getNombre() +
                                        ".</p><p>Motivo: " + guardada.getMotivoCancelacion() + "</p>"
                        );
                    } catch (Exception e) {
                        System.err.println("No se pudo enviar correo institucional al admin: " + e.getMessage());
                    }
                }
                // Si canceladoPor es "sistema", este método (actualizarReserva) no es el que se usa —
                // el job cancelarReservasPorVencimientoPago maneja ese caso por separado.
            }

            return guardada;
        }).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }

    public Reserva confirmarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        reserva.setEstado(EstadoReserva.confirmada);
        reserva.setFechaLimitePago(LocalDateTime.now().plusMinutes(15));

        Reserva guardada = reservaRepository.save(reserva);

        // NUEVO: notificación interna al usuario
        try {
            NotificacionDTO dto = new NotificacionDTO();
            dto.setIdUsuario(guardada.getUsuario().getId());
            dto.setMensaje("Tu reserva en " + guardada.getCancha().getNombre() +
                    " del " + guardada.getFechaReserva() +
                    " fue confirmada. Tienes 15 minutos para realizar el pago.");
            dto.setIdReserva(guardada.getId()); // NUEVO
            notiService.enviar(dto);
        } catch (Exception e) {
            System.err.println("No se pudo notificar al usuario (confirmación): " + e.getMessage());
        }

        try {
            emailService.enviarCorreoConfirmacionPendientePago(
                    guardada.getUsuario().getEmail(),
                    guardada.getUsuario().getNombre(),
                    guardada.getCancha().getNombre(),
                    guardada.getFechaReserva().toString(),
                    String.valueOf(guardada.getHoraInicio()),
                    String.valueOf(guardada.getHoraFin())
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar correo de confirmación: " + e.getMessage());
        }

        return guardada;
    }

    // NUEVO: usado por PagoController en los 3 puntos donde se confirma un pago
    public Reserva confirmarPago(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reserva.getEstado() != EstadoReserva.confirmada) {
            throw new IllegalStateException("La reserva no está en estado confirmada");
        }

        if (reserva.getFechaLimitePago() != null && reserva.getFechaLimitePago().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El plazo de pago ya venció");
        }

        reserva.setEstado(EstadoReserva.pagada);
        Reserva guardada = reservaRepository.save(reserva);

        String cancha = guardada.getCancha() != null ? guardada.getCancha().getNombre() : "tu cancha";

// Notificación interna al usuario
        try {
            NotificacionDTO dto = new NotificacionDTO();
            dto.setIdUsuario(guardada.getUsuario().getId());
            dto.setMensaje("Tu pago para la reserva en " + cancha + " del " + guardada.getFechaReserva() +
                    " fue confirmado exitosamente.");
            dto.setIdReserva(guardada.getId()); // NUEVO
            notiService.enviar(dto);
        } catch (Exception e) {
            System.err.println("No se pudo notificar al usuario (pago): " + e.getMessage());
        }

// Notificación interna a los admins
        try {
            notiService.notificarAdmins(
                    guardada.getUsuario().getNombre() + " pagó su reserva en " + cancha +
                            " del " + guardada.getFechaReserva() +
                            " (" + guardada.getHoraInicio() + " - " + guardada.getHoraFin() + ")."
            );
        } catch (Exception e) {
            System.err.println("No se pudo notificar a los admins (pago): " + e.getMessage());
        }

        try {
            emailService.enviarCorreoPagoExitoso(
                    guardada.getUsuario().getEmail(),
                    guardada.getUsuario().getNombre(),
                    cancha,
                    guardada.getFechaReserva().toString(),
                    String.valueOf(guardada.getHoraInicio()),
                    String.valueOf(guardada.getHoraFin())
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar correo de pago exitoso: " + e.getMessage());
        }

// Correo institucional al admin (preparado, inactivo hasta configurar club.email.institucional)
        try {
            emailService.enviarCorreoInstitucionalAdmin(
                    "Pago confirmado - " + guardada.getUsuario().getNombre(),
                    "<p>El usuario <strong>" + guardada.getUsuario().getNombre() + "</strong> confirmó el pago de su reserva en " +
                            cancha + " del " + guardada.getFechaReserva() + ".</p>"
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar correo institucional (pago): " + e.getMessage());
        }

        return guardada;
    }

    @Transactional
    public void cancelarReserva(Integer id) {
        ConfiguracionClub config = configuracionClubService.obtenerConfiguracion();

        if (Boolean.FALSE.equals(config.getPermitirCancelaciones())) {
            throw new IllegalStateException("Las cancelaciones están deshabilitadas actualmente.");
        }

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        reserva.setEstado(EstadoReserva.cancelada);
        reserva.setCanceladoPor("cliente");
        reserva.setMotivoCancelacion("Cancelada por el cliente");
        reserva.setMetodoPago(null);
        reserva.setComprobanteUrl(null);

        Reserva guardada = reservaRepository.save(reserva);

        // Notificar SOLO al admin (mismo patrón que en actualizarReserva)
        Usuario usuario = guardada.getUsuario();
        Cancha cancha = guardada.getCancha();

        try {
            notiService.notificarAdmins(
                    usuario.getNombre() + " canceló su reserva del " + guardada.getFechaReserva() +
                            " (" + guardada.getHoraInicio() + " - " + guardada.getHoraFin() + ")" +
                            " en " + cancha.getNombre() + ". Motivo: " + guardada.getMotivoCancelacion()
            );
        } catch (Exception e) { 
            System.err.println("No se pudo notificar a los admins: " + e.getMessage());
        }

        try {
            emailService.enviarCorreoInstitucionalAdmin(
                    "Cancelación de reserva - " + usuario.getNombre(),
                    "<p>El usuario <strong>" + usuario.getNombre() + "</strong> canceló su reserva del " +
                            guardada.getFechaReserva() + " en " + cancha.getNombre() +
                            ".</p><p>Motivo: " + guardada.getMotivoCancelacion() + "</p>"
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar correo institucional al admin: " + e.getMessage());
        }
    }
    public Reserva findById(Integer id) {
        return reservaRepository.findById(id).orElse(null);
    }

    public Reserva guardarReserva(Reserva reserva) {
        return reservaRepository.save(reserva);
    }


}