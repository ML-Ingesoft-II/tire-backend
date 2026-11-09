package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.RespuestaToken;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Representacion de salida del token emitido tras un inicio de sesion.
 */
public record TokenResponseDto(
        String accessToken,
        String tipo,
        long expiracionSegundos,
        RolUsuario rol,
        UUID usuarioId,
        String nombre) {

    public static TokenResponseDto desde(RespuestaToken respuesta) {
        return new TokenResponseDto(
                respuesta.accessToken(),
                respuesta.tipo(),
                respuesta.expiracionSegundos(),
                respuesta.rol(),
                respuesta.usuarioId(),
                respuesta.nombre());
    }
}
