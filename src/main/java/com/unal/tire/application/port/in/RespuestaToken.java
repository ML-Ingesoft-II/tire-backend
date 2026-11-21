package com.unal.tire.application.port.in;

import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Respuesta devuelta tras un inicio de sesion o una renovacion exitosa.
 *
 * @param accessToken         token JWT de acceso de vida corta.
 * @param refreshToken        token JWT de larga duracion para renovar la sesion.
 * @param tipo                tipo de token (siempre "Bearer").
 * @param expiracionSegundos  vigencia del access token en segundos.
 * @param rol                 rol del usuario autenticado.
 * @param usuarioId           identificador del usuario.
 * @param nombre              nombre del usuario.
 * @param flotaId             flota del usuario (null para conductor particular).
 * @param nombreFlota         nombre de la flota (null para conductor particular).
 * @param debeCambiarPassword true si el usuario aun usa su contrasena inicial.
 */
public record RespuestaToken(
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
}
