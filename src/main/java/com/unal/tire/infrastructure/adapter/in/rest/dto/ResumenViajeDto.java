package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.TipoEvento;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Resumen de un viaje ({@code ResumenViaje}): duracion y conteos de eventos.
 */
public record ResumenViajeDto(
        int duracionMinutos,
        int microsuenos,
        int rostroNoVisible,
        int alertasCriticas) {

    public static ResumenViajeDto desde(Viaje v) {
        LocalDateTime fin = v.getFechaFin() != null ? v.getFechaFin() : LocalDateTime.now();
        int duracion = v.getFechaInicio() != null
                ? (int) Duration.between(v.getFechaInicio(), fin).toMinutes()
                : 0;

        int micro = 0;
        int rostro = 0;
        int alertas = 0;
        for (EventoFatiga e : v.getEventosFatiga()) {
            if (e.getTipoEvento() == TipoEvento.MICROSUENO) {
                micro++;
                if (!e.fueConfirmado()) {
                    alertas++;
                }
            } else if (e.getTipoEvento() == TipoEvento.ROSTRO_NO_VISIBLE) {
                rostro++;
            }
        }
        return new ResumenViajeDto(Math.max(duracion, 0), micro, rostro, alertas);
    }
}
