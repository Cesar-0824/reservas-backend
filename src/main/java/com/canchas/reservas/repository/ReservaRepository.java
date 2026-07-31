package com.canchas.reservas.repository;

import com.canchas.reservas.model.Reserva;
import com.canchas.reservas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByUsuario(Usuario usuario);
    List<Reserva> findByFechaReserva(LocalDate fecha);
}
