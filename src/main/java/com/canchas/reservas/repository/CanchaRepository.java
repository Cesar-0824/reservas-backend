package com.canchas.reservas.repository;

import com.canchas.reservas.model.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanchaRepository extends JpaRepository<Cancha, Integer> {
    // Aquí puedes agregar filtros como: findByTipo(), findByEstado(), etc.
}
