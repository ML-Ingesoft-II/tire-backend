package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para confirmar "Estoy bien" tras una escalada
 * ({@code ConfirmarAlertaRequest}).
 */
public record ConfirmarAlertaDto(
        @NotNull(message = "El tiempo de respuesta es obligatorio")
        @DecimalMin(value = "0.0", message = "El tiempo de respuesta debe ser >= 0")
        Double tiempoRespuestaSegundos) {
}
