package com.canchas.reservas.service;

import com.canchas.reservas.model.Cancha;
import com.canchas.reservas.model.EstadoReserva;
import com.canchas.reservas.model.Reserva;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.repository.CanchaRepository;
import com.canchas.reservas.repository.ReservaRepository;
import com.canchas.reservas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        return reservaRepository.save(reserva);
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

    public Reserva actualizarReserva(Integer id, Reserva reservaActualizado) {
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setFechaReserva(reservaActualizado.getFechaReserva());
            reserva.setHoraInicio(reservaActualizado.getHoraInicio());
            reserva.setHoraFin(reservaActualizado.getHoraFin());
            reserva.setEstado(reservaActualizado.getEstado());
            reserva.setMetodoPago(reservaActualizado.getMetodoPago());
            reserva.setComprobanteUrl(reservaActualizado.getComprobanteUrl());
            reserva.setMontoTotal(reservaActualizado.getMontoTotal());

            // Cargar Usuario y Cancha por su id para asignarlos
            Integer usuarioId = reservaActualizado.getUsuario().getId();
            Integer canchaId = reservaActualizado.getCancha().getId();

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            Cancha cancha = canchaRepository.findById(canchaId)
                    .orElseThrow(() -> new IllegalArgumentException("Cancha no encontrada"));

            reserva.setUsuario(usuario);
            reserva.setCancha(cancha);

            return reservaRepository.save(reserva);
        }).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }



    public void cancelarReserva(Integer id) {
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
