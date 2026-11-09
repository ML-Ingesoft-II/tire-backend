package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para registrar un evento de fatiga. Las coordenadas son
 * opcionales (pueden ser null si no hay senal GPS).
 */
public record EventoFatigaDto(
        @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180")
        Double longitud,

        @NotNull(message = "La duracion de ojos cerrados es obligatoria")
        @DecimalMin(value = "0.0", message = "La duracion no puede ser negativa")
        Double duracionOjosCerradosSegundos,

        boolean confirmadoPorConductor) {
}
