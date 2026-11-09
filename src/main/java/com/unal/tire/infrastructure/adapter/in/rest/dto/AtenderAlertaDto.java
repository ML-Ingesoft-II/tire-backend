package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para que el despachador atienda una alerta critica.
 */
public record AtenderAlertaDto(
        @NotBlank(message = "Las notas del despachador son obligatorias")
        @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
        String notas) {
}
