package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * Datos de entrada para iniciar un viaje ({@code ViajeRequest}). Todos los
 * campos son opcionales.
 */
public record IniciarViajeDto(
        @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90")
        Double latitudInicial,

        @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180")
        Double longitudInicial,

        String origen,
        String destino) {
}
