package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para registrar telemetria de posicion.
 */
public record TelemetriaDto(
        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180")
        Double longitud) {
}
