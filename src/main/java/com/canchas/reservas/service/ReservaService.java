package com.canchas.reservas.service;

import com.canchas.reservas.DTO.NotificacionDTO;
import com.canchas.reservas.model.*;
import com.canchas.reservas.repository.CanchaRepository;
import com.canchas.reservas.repository.ReservaRepository;
import com.canchas.reservas.repository.UsuarioRepository;
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

        if (reserva.getCancha() == null ||
                reserva.getCancha().getId() == null ||
                !canchaRepository.existsById(reserva.getCancha().getId())) {
            throw new IllegalArgumentException("Cancha inválida o no existe");
        }

        validarReglasDeReserva(reserva);

        return reservaRepository.save(reserva);
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
        return reservaRepository.findByFechaReserva(fecha);
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
                try {
                    NotificacionDTO dto = new NotificacionDTO();
                    dto.setIdUsuario(usuario.getId());
                    dto.setMensaje("Tu reserva del " + guardada.getFechaReserva() +
                            " fue cancelada. Motivo: " + guardada.getMotivoCancelacion());
                    notiService.enviar(dto);
                } catch (Exception e) {
                    System.err.println("No se pudo enviar notificación: " + e.getMessage());
                }
            }

            return guardada;
        }).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }

    // Usado por el CLIENTE para cancelar su propia reserva — sí respeta permitirCancelaciones
    public void cancelarReserva(Integer id) {
        ConfiguracionClub config = configuracionClubService.obtenerConfiguracion();

        if (Boolean.FALSE.equals(config.getPermitirCancelaciones())) {
            throw new IllegalStateException("Las cancelaciones están deshabilitadas actualmente.");
        }

        if (!reservaRepository.existsById(id)) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }

        reservaRepository.deleteById(id);
    }

    public Reserva findById(Integer id) {
        Optional<Reserva> optionalReserva = reservaRepository.findById(id);
        return optionalReserva.orElse(null);
    }

    public Reserva guardarReserva(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

}