package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.ConductorEnViaje;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import com.unal.tire.domain.valueobject.NivelAlerta;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conductor en viaje en el estado de la flota ({@code ConductorEnViaje}).
 */
public record ConductorEnViajeDto(
        ConductorResumenDto conductor,
        UUID viajeId,
        EstadoMonitoreo estado,
        NivelAlerta nivelAlerta,
        LocalDateTime fechaInicio,
        LocalDateTime ultimaSenalEn,
        Double ultimaLatitud,
        Double ultimaLongitud,
        Double velocidadKmh,
        UUID alertaAbiertaId) {

    public static ConductorEnViajeDto desde(ConductorEnViaje c) {
        return new ConductorEnViajeDto(
                ConductorResumenDto.desde(c.conductor()),
                c.viajeId(),
                c.estado(),
                c.nivelAlerta(),
                c.fechaInicio(),
                c.ultimaSenalEn(),
                c.ultimaLatitud(),
                c.ultimaLongitud(),
                c.velocidadKmh(),
                c.alertaAbiertaId());
    }
}
