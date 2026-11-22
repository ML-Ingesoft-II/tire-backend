package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.valueobject.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de entrada para actualizar parcialmente (PATCH) un conductor
 * ({@code ActualizarConductorRequest}). Todos los campos son opcionales.
 */
public record ActualizarConductorDto(
        String nombres,
        String apellidos,
        TipoDocumento tipoDocumento,

        @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "El número de documento no es válido")
        String numeroDocumento,

        @Email(message = "El correo no tiene un formato válido")
        String correo,

        String telefono,
        String placaVehiculo,
        Boolean activo) {
}
