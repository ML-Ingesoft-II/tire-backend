package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada para el inicio de sesion.
 *
 * @param email    correo electronico (obligatorio).
 * @param password contrasena (obligatorio).
 */
public record LoginRequestDto(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
