package com.unal.tire.application.port.out;

import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Claims de autenticacion extraidos de un token validado.
 *
 * @param usuarioId identificador del usuario.
 * @param email     correo electronico del usuario.
 * @param rol       rol funcional del usuario.
 * @param flotaId   flota a la que pertenece el usuario (puede ser null).
 */
public record ClaimsAutenticacion(
        UUID usuarioId,
        String email,
        RolUsuario rol,
        UUID flotaId) {
}
