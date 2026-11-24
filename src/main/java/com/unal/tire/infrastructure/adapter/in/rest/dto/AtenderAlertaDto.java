package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para atender una alerta ({@code AtenderAlertaRequest}). Las
 * notas son opcionales.
 */
public record AtenderAlertaDto(
        @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
        String notas) {
}
