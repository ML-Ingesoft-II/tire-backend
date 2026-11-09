package com.unal.tire.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos para solicitar la recuperacion de contrasena (HU-04).
 */
@Schema(description = "Solicitud de recuperacion de contrasena")
public record RecuperacionPasswordDto(
        @Schema(description = "Correo electronico registrado", example = "conductor@tire.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es invalido")
        String email
) {
}
