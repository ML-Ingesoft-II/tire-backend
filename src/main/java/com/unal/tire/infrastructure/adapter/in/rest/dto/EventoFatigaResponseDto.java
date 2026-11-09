package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacion de salida de un evento de fatiga.
 */
public record EventoFatigaResponseDto(
        UUID id,
        UUID viajeId,
        LocalDateTime fechaHora,
        Double latitud,
        Double longitud,
        double duracionOjosCerradosSegundos,
        boolean confirmadoPorConductor,
        CondicionClimatica climaEnRuta) {

    public static EventoFatigaResponseDto desde(EventoFatiga e) {
        Ubicacion u = e.getUbicacion();
        return new EventoFatigaResponseDto(
                e.getId(),
                e.getViajeId(),
                e.getFechaHora(),
                u != null ? u.latitud() : null,
                u != null ? u.longitud() : null,
                e.getDuracionOjosCerradosSegundos(),
                e.isConfirmadoPorConductor(),
                e.getClimaEnRuta());
    }
}
