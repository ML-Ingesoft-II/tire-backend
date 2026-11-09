package com.unal.tire.application.port.in;

import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Respuesta devuelta tras un inicio de sesion exitoso.
 *
 * @param accessToken         token JWT de acceso.
 * @param tipo                tipo de token (siempre "Bearer").
 * @param expiracionSegundos  vigencia del token en segundos.
 * @param rol                 rol del usuario autenticado.
 * @param usuarioId           identificador del usuario.
 * @param nombre              nombre del usuario.
 */
public record RespuestaToken(
        String accessToken,
        String tipo,
        long expiracionSegundos,
        RolUsuario rol,
        UUID usuarioId,
        String nombre) {
}
