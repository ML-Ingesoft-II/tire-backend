package com.unal.tire.application.port.in;

import com.unal.tire.domain.valueobject.TipoEvento;
import java.time.LocalDateTime;

/**
 * Comando para registrar un evento de fatiga en un viaje. Las coordenadas son
 * opcionales. {@code respondio} y {@code tiempoRespuestaSegundos} solo aplican
 * a MICROSUENO.
 */
public record ComandoEventoFatiga(
        TipoEvento tipoEvento,
        LocalDateTime fechaHoraDeteccion,
        double duracionSegundos,
        Boolean respondio,
        Double tiempoRespuestaSegundos,
        Double latitud,
        Double longitud) {
}
