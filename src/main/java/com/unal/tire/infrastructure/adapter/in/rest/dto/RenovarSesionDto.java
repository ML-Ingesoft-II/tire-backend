package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada para renovar la sesion ({@code RenovarSesionRequest}).
 */
public record RenovarSesionDto(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken) {
}
