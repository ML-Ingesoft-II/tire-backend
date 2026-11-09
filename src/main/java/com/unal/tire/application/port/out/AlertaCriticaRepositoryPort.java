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
}
