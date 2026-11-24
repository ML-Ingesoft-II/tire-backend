package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Representacion de salida de un viaje ({@code ViajeResponse}). La lista de
 * {@code eventos} solo se incluye en {@code GET /viajes/&#123;id&#125;} y en las
 * respuestas de escritura.
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
        LocalDateTime ultimaSenalEn,
        Double velocidadKmh,
        CondicionClimaticaDto ultimoClima,
        ResumenViajeDto resumen,
        List<EventoFatigaResponseDto> eventos) {

    /** Viaje con sus eventos (detalle y respuestas de escritura). */
    public static ViajeResponseDto desde(Viaje v) {
        return construir(v, true);
    }

    /** Viaje sin la lista de eventos (listado paginado). */
    public static ViajeResponseDto desdeSinEventos(Viaje v) {
        return construir(v, false);
    }

    private static ViajeResponseDto construir(Viaje v, boolean conEventos) {
        Ubicacion u = v.getUltimaUbicacion();
        List<EventoFatigaResponseDto> eventos = conEventos
                ? v.getEventosFatiga().stream().map(EventoFatigaResponseDto::desde).toList()
                : null;
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
                v.getUltimaSenalEn(),
                v.getVelocidadKmh(),
                CondicionClimaticaDto.desde(v.getUltimoClima()),
                ResumenViajeDto.desde(v),
                eventos);
    }
}
