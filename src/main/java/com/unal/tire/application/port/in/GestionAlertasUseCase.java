package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.Viaje;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada para la operacion del despachador: estado de flota y
 * atencion de alertas criticas.
 */
public interface GestionAlertasUseCase {

    List<Viaje> listarViajesActivos();

    List<AlertaCritica> listarAlertasAbiertas();

    List<AlertaCritica> listarIncidentesAtendidos();

    List<AlertaCritica> listarIncidentesAtendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    AlertaCritica atenderAlerta(UUID alertaId, String notas);
}
