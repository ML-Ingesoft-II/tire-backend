package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Representacion de salida de un usuario recien registrado (sin exponer el
 * hash de la contrasena).
 */
public record RegistroResponseDto(
        UUID id,
        String email,
        String nombre,
        RolUsuario rol,
        UUID flotaId,
        boolean activo) {

    public static RegistroResponseDto desde(Usuario usuario) {
        return new RegistroResponseDto(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getFlotaId(),
                usuario.isActivo());
    }
}
