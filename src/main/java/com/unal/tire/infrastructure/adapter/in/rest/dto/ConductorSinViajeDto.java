package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.ConductorSinViaje;
import java.time.LocalDateTime;

/**
 * Conductor sin viaje en el estado de la flota ({@code ConductorSinViaje}).
 */
public record ConductorSinViajeDto(
        ConductorResumenDto conductor,
        LocalDateTime fechaUltimoViaje) {

    public static ConductorSinViajeDto desde(ConductorSinViaje c) {
        return new ConductorSinViajeDto(
                ConductorResumenDto.desde(c.conductor()),
                c.fechaUltimoViaje());
    }
}
