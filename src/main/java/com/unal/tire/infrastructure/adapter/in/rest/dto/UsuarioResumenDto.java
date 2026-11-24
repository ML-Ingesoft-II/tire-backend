package com.unal.tire.infrastructure.adapter.in.rest.dto;

import java.util.UUID;

/**
 * Resumen de un usuario ({@code UsuarioResumen}).
 */
public record UsuarioResumenDto(UUID id, String nombre) {

    public static UsuarioResumenDto de(UUID id, String nombre) {
        if (id == null) {
            return null;
        }
        return new UsuarioResumenDto(id, nombre);
    }
}
