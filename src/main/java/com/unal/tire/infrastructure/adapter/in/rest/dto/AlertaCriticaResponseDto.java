package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacion de salida de una alerta critica.
 */
public record AlertaCriticaResponseDto(
        UUID id,
        UUID viajeId,
        UUID conductorId,
        UUID flotaId,
        LocalDateTime fechaApertura,
        LocalDateTime fechaAtencion,
        Double latitud,
        Double longitud,
        boolean atendida,
        String notasDespachador) {

    public static AlertaCriticaResponseDto desde(AlertaCritica a) {
        Ubicacion u = a.getUltimaUbicacion();
        return new AlertaCriticaResponseDto(
                a.getId(),
                a.getViajeId(),
                a.getConductorId(),
                a.getFlotaId(),
                a.getFechaApertura(),
                a.getFechaAtencion(),
                u != null ? u.latitud() : null,
                u != null ? u.longitud() : null,
                a.isAtendida(),
                a.getNotasDespachador());
    }
}
