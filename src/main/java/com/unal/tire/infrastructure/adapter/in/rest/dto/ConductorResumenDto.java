package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.ConductorResumen;
import java.util.UUID;

/**
 * Resumen del conductor para flota, alertas e historial ({@code ConductorResumen}).
 */
public record ConductorResumenDto(
        UUID id,
        String nombreCompleto,
        String placaVehiculo,
        String telefono) {

    public static ConductorResumenDto desde(ConductorResumen c) {
        if (c == null) {
            return null;
        }
        return new ConductorResumenDto(c.id(), c.nombreCompleto(), c.placaVehiculo(), c.telefono());
    }
}
