package com.unal.tire.application.port.in;

import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.UUID;

/**
 * Comando de entrada para registrar un nuevo usuario.
 *
 * @param nombre   nombre del usuario.
 * @param email    correo electronico (identificador de acceso).
 * @param password contrasena en texto plano (se hashea en el servicio).
 * @param rol      rol funcional.
 * @param flotaId  flota asociada (opcional, puede ser null).
 */
public record ComandoRegistro(
        String nombre,
        String email,
        String password,
        RolUsuario rol,
        UUID flotaId) {
}
