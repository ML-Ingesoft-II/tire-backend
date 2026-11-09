package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada para iniciar un viaje. Las coordenadas iniciales son
 * opcionales.
 */
public record IniciarViajeDto(
        @NotBlank(message = "El origen es obligatorio")
        String origen,

        @NotBlank(message = "El destino es obligatorio")
        String destino,

        @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90")
        Double latitudInicial,

        @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180")
        Double longitudInicial) {
}
