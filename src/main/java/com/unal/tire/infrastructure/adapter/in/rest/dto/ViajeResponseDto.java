package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Representacion de salida de un viaje con sus eventos de fatiga.
 */
public record ViajeResponseDto(
        UUID id,
        UUID conductorId,
        String origen,
        String destino,
        EstadoViaje estado,
        NivelAlerta nivelAlerta,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Double ultimaLatitud,
        Double ultimaLongitud,
        CondicionClimatica ultimoClima,
        List<EventoFatigaResponseDto> eventosFatiga) {

    public static ViajeResponseDto desde(Viaje v) {
        Ubicacion u = v.getUltimaUbicacion();
        List<EventoFatigaResponseDto> eventos = v.getEventosFatiga().stream()
                .map(EventoFatigaResponseDto::desde)
                .toList();
        return new ViajeResponseDto(
                v.getId(),
                v.getConductorId(),
                v.getOrigen(),
                v.getDestino(),
                v.getEstado(),
                v.getNivelAlerta(),
                v.getFechaInicio(),
                v.getFechaFin(),
                u != null ? u.latitud() : null,
                u != null ? u.longitud() : null,
                v.getUltimoClima(),
                eventos);
    }
}
