package com.unal.tire.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para registrar un conductor particular ({@code RegistroRequest}).
 */
public record RegistroRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        Boolean aceptaTratamientoDatos) {

    @AssertTrue(message = "Debes aceptar el tratamiento de datos")
    public boolean isAceptaTratamientoDatosValido() {
        return Boolean.TRUE.equals(aceptaTratamientoDatos);
    }
}
