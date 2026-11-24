package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para definir la nueva contrasena con el token del enlace
 * ({@code ConfirmarRecuperacionRequest}).
 */
public record ConfirmarRecuperacionDto(
        @NotBlank(message = "El token es obligatorio")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
        String passwordNueva) {
}
