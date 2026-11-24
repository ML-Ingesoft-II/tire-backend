package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.AlertaCritica;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de alertas criticas.
 */
public interface AlertaCriticaRepositoryPort {

    AlertaCritica guardar(AlertaCritica alerta);

    Optional<AlertaCritica> buscarPorId(UUID id);

    List<AlertaCritica> listarPorAtendida(boolean atendida);

    /**
     * Alertas de una flota filtradas por estado de atencion, mas reciente
     * (fecha de apertura) primero.
     */
    List<AlertaCritica> listarPorFlotaYAtendida(UUID flotaId, boolean atendida);

    /** Todas las alertas de una flota (atendidas y abiertas). */
    List<AlertaCritica> listarPorFlota(UUID flotaId);

    /** Alerta asociada a un evento de fatiga, si existe. */
    Optional<AlertaCritica> buscarPorEventoId(UUID eventoId);
}
