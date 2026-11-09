package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para el cambio de contrasena de un usuario autenticado.
 *
 * @param passwordActual contrasena vigente (obligatorio).
 * @param passwordNueva  nueva contrasena, minimo 8 caracteres (obligatorio).
 */
public record CambioPasswordDto(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
        String passwordNueva) {
}
