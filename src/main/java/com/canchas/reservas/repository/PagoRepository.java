package com.canchas.reservas.repository;

import com.canchas.reservas.model.Pago;
import com.canchas.reservas.model.Pago.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByReservaIdAndEstadoIn(Integer reservaId, List<EstadoPago> estados);

    boolean existsByReservaId(Integer reservaId);

}
