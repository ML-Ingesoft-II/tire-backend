package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacion de salida de un evento de fatiga ({@code EventoFatigaResponse}).
 */
public record EventoFatigaResponseDto(
        UUID id,
        UUID viajeId,
        TipoEvento tipoEvento,
        LocalDateTime fechaHora,
        double duracionSegundos,
        RespuestaConductorDto respuesta,
        boolean confirmado,
        Double latitud,
        Double longitud,
        UUID alertaId) {

    public static EventoFatigaResponseDto desde(EventoFatiga e) {
        return desde(e, null);
    }

    public static EventoFatigaResponseDto desde(EventoFatiga e, UUID alertaId) {
        Ubicacion u = e.getUbicacion();
        return new EventoFatigaResponseDto(
                e.getId(),
                e.getViajeId(),
                e.getTipoEvento(),
                e.getFechaHora(),
                e.getDuracionSegundos(),
                RespuestaConductorDto.desde(e),
                e.fueConfirmado(),
                u != null ? u.latitud() : null,
                u != null ? u.longitud() : null,
                alertaId);
    }
}
