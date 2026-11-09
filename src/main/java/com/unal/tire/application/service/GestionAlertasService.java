package com.unal.tire.application.service;

import com.unal.tire.application.port.in.GestionAlertasUseCase;
import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoViaje;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de operacion del despachador: panorama de flota y atencion de
 * alertas criticas.
 */
@Service
public class GestionAlertasService implements GestionAlertasUseCase {

    private final ViajeRepositoryPort viajeRepository;
    private final AlertaCriticaRepositoryPort alertaRepository;

    public GestionAlertasService(
            ViajeRepositoryPort viajeRepository,
            AlertaCriticaRepositoryPort alertaRepository) {
        this.viajeRepository = viajeRepository;
        this.alertaRepository = alertaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viaje> listarViajesActivos() {
        return viajeRepository.listarPorEstado(EstadoViaje.EN_CURSO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaCritica> listarAlertasAbiertas() {
        return alertaRepository.listarPorAtendida(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaCritica> listarIncidentesAtendidos() {
        return alertaRepository.listarPorAtendida(true);
    }

    @Override
    @Transactional
    public AlertaCritica atenderAlerta(UUID alertaId, String notas) {
        AlertaCritica alerta = alertaRepository.buscarPorId(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta critica no encontrada"));
        alerta.atender(notas);
        return alertaRepository.guardar(alerta);
    }
}
