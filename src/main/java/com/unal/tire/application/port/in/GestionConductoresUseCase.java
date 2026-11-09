package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.Conductor;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada del caso de uso de gestion de conductores.
 */
public interface GestionConductoresUseCase {

    Conductor crear(ComandoCrearConductor comando);

    List<Conductor> listarTodos();

    Conductor obtenerPorId(UUID id);

    Conductor actualizar(UUID id, ComandoActualizarConductor comando);

    Conductor desactivar(UUID id);
}
