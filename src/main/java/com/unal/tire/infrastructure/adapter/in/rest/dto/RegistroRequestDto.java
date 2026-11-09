package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.valueobject.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Datos de entrada para el registro de un usuario.
 *
 * @param nombre   nombre del usuario (obligatorio).
 * @param email    correo electronico valido (obligatorio).
 * @param password contrasena, minimo 8 caracteres (obligatorio).
 * @param rol      rol funcional (obligatorio).
 * @param flotaId  flota asociada (opcional).
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

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol,

        UUID flotaId) {
}
