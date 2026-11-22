package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.valueobject.TipoEvento;

/**
 * Lo que hizo el conductor ante el evento ({@code RespuestaConductor}).
 *
 * @param tipo     RESPONDIO / NO_RESPONDIO / VOLVIO_A_CAMARA.
 * @param segundos tiempo de respuesta o tiempo hasta volver a la camara.
 */
public record RespuestaConductorDto(String tipo, Double segundos) {

    public static RespuestaConductorDto desde(EventoFatiga e) {
        if (e.getTipoEvento() == TipoEvento.ROSTRO_NO_VISIBLE) {
            return new RespuestaConductorDto("VOLVIO_A_CAMARA", e.getDuracionSegundos());
        }
        if (Boolean.TRUE.equals(e.getRespondio())) {
            return new RespuestaConductorDto("RESPONDIO", e.getTiempoRespuestaSegundos());
        }
        return new RespuestaConductorDto("NO_RESPONDIO", null);
    }
}
