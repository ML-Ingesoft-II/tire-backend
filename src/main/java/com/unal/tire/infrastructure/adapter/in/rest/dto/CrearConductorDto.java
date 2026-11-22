package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.valueobject.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de entrada para crear un conductor de flota ({@code ConductorRequest}).
 * La flota se toma del token del despachador.
 */
public record CrearConductorDto(
        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "El número de documento no es válido")
        String numeroDocumento,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,

        String telefono,

        String placaVehiculo) {
}
