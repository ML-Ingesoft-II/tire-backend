package com.unal.tire.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;

/**
 * Acceso denegado por la invariante HU-03: el usuario aun usa su contrasena
 * inicial y debe cambiarla antes de usar otros endpoints.
 *
 * <p>El {@link CustomAccessDeniedHandler} la traduce a un 403 con el tipo
 * {@code .../problems/password-change-required}.</p>
 */
public class CambioPasswordRequeridoSecurityException extends AccessDeniedException {

    public CambioPasswordRequeridoSecurityException() {
        super("Debes cambiar tu contraseña inicial antes de continuar.");
    }
}
