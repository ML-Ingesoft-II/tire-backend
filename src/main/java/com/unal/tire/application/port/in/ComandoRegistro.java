package com.unal.tire.application.port.in;

/**
 * Comando de entrada para registrar un conductor particular (ruta publica).
 *
 * <p>Siempre crea un usuario con rol CONDUCTOR y sin flota, segun el contrato.</p>
 *
 * @param nombre   nombre del usuario.
 * @param email    correo electronico (identificador de acceso).
 * @param password contrasena en texto plano (se hashea en el servicio).
 */
public record ComandoRegistro(
        String nombre,
        String email,
        String password) {
}
