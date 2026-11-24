package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.RespuestaToken;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Representacion de salida del par de tokens y los datos de inicio de cada rol.
 * Campos segun el esquema {@code TokenResponse} del contrato.
 */
public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        String tipo,
        long expiracionSegundos,
        RolUsuario rol,
        UUID usuarioId,
        String nombre,
        UUID flotaId,
        String nombreFlota,
        boolean debeCambiarPassword) {

    public static TokenResponseDto desde(RespuestaToken r) {
        return new TokenResponseDto(
                r.accessToken(),
                r.refreshToken(),
                r.tipo(),
                r.expiracionSegundos(),
                r.rol(),
                r.usuarioId(),
                r.nombre(),
                r.flotaId(),
                r.nombreFlota(),
                r.debeCambiarPassword());
    }
}
