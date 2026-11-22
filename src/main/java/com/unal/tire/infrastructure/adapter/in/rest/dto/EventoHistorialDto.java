package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.EventoHistorial;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento del historial ({@code EventoHistorial}): un {@code EventoFatigaResponse}
 * mas el conductor y el estado de atencion de la alerta asociada.
 */
public record EventoHistorialDto(
        UUID id,
        UUID viajeId,
        TipoEvento tipoEvento,
        LocalDateTime fechaHora,
        double duracionSegundos,
        RespuestaConductorDto respuesta,
        boolean confirmado,
        Double latitud,
        Double longitud,
        UUID alertaId,
        ConductorResumenDto conductor,
        String atencion) {

    public static EventoHistorialDto desde(EventoHistorial h) {
        EventoFatiga e = h.evento();
        Ubicacion u = e.getUbicacion();
        return new EventoHistorialDto(
                e.getId(),
                e.getViajeId(),
                e.getTipoEvento(),
                e.getFechaHora(),
                e.getDuracionSegundos(),
                RespuestaConductorDto.desde(e),
                e.fueConfirmado(),
                u != null ? u.latitud() : null,
                u != null ? u.longitud() : null,
                null,
                ConductorResumenDto.desde(h.conductor()),
                h.atencion());
    }
}
