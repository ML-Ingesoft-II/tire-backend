package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.valueobject.TipoEvento;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Datos de entrada para registrar un evento de fatiga ({@code EventoFatigaRequest}).
 */
public record EventoFatigaDto(
        @NotNull(message = "El tipo de evento es obligatorio")
        TipoEvento tipoEvento,

        @NotNull(message = "La fecha/hora de detección es obligatoria")
        LocalDateTime fechaHoraDeteccion,

        @NotNull(message = "La duración es obligatoria")
        @DecimalMin(value = "0.0", message = "La duración no puede ser negativa")
        Double duracionSegundos,

        Boolean respondio,

        @DecimalMin(value = "0.0", message = "El tiempo de respuesta debe ser >= 0")
        @DecimalMax(value = "5.0", message = "El tiempo de respuesta debe ser <= 5")
        Double tiempoRespuestaSegundos,

        @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180")
        Double longitud) {
}
