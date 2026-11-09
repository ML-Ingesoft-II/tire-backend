package com.unal.tire.infrastructure.adapter.in.rest.dto;

/**
 * Datos de entrada para actualizar parcialmente (PATCH) un conductor. Todos los
 * campos son opcionales; los nulos no modifican el valor existente.
 */
public record ActualizarConductorDto(
        String nombre,
        String telefono,
        Boolean activo) {
}
