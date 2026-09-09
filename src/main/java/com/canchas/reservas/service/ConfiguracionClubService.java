package com.canchas.reservas.service;

import com.canchas.reservas.model.ConfiguracionClub;
import com.canchas.reservas.repository.ConfiguracionClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionClubService {

    @Autowired
    private ConfiguracionClubRepository repository;

    public ConfiguracionClub obtenerConfiguracion() {
        return repository.findById(1)
                .orElseGet(() -> {
                    ConfiguracionClub nueva = new ConfiguracionClub();
                    nueva.setId(1);
                    return repository.save(nueva);
                });
    }

    public ConfiguracionClub guardarGeneral(String nombreClub, String emailContacto, String telefono, String horaApertura, String horaCierre) {
        ConfiguracionClub config = obtenerConfiguracion();
        config.setNombreClub(nombreClub);
        config.setEmailContacto(emailContacto);
        config.setTelefono(telefono);
        config.setHoraApertura(horaApertura);
        config.setHoraCierre(horaCierre);
        return repository.save(config);
    }

    public ConfiguracionClub guardarReservas(Double duracionMinima, Double duracionMaxima, Integer anticipacionMaximaDias, Boolean permitirCancelaciones) {
        if (duracionMinima % 0.5 != 0 || duracionMaxima % 0.5 != 0) {
            throw new IllegalArgumentException("Las duraciones deben ser múltiplos de 0.5 (30 minutos).");
        }
        if (duracionMinima > duracionMaxima) {
            throw new IllegalArgumentException("La duración mínima no puede ser mayor que la máxima.");
        }

        ConfiguracionClub config = obtenerConfiguracion();
        config.setDuracionMinima(duracionMinima);
        config.setDuracionMaxima(duracionMaxima);
        config.setAnticipacionMaximaDias(anticipacionMaximaDias);
        config.setPermitirCancelaciones(permitirCancelaciones);
        return repository.save(config);
    }

    public ConfiguracionClub guardarNotificaciones(Boolean nuevasReservas, Boolean pagosExitosos, Boolean reservasCanceladas) {
        ConfiguracionClub config = obtenerConfiguracion();
        config.setNotifNuevasReservas(nuevasReservas);
        config.setNotifPagosExitosos(pagosExitosos);
        config.setNotifReservasCanceladas(reservasCanceladas);
        return repository.save(config);
    }
}