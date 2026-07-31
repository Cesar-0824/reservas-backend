package com.canchas.reservas.service;

import com.canchas.reservas.model.Pago;
import com.canchas.reservas.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;

    public PagoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    public Pago guardarPago(Pago pago) {
        return pagoRepository.save(pago);
    }

    public boolean existePagoParaReserva(Integer reservaId) {
        return pagoRepository.existsByReservaId(reservaId);
    }

    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }
}
