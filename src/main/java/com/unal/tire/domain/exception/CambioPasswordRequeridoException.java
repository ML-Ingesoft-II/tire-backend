package com.unal.tire.domain.exception;

/**
 * Excepcion de dominio para la invariante HU-03: mientras un conductor de flota
 * no cambie su contrasena inicial ({@code debeCambiarPassword=true}), no puede
 * usar ningun endpoint distinto de {@code PUT /auth/contrasena} y
 * {@code POST /auth/renovar}.
 *
 * <p>Se traduce a HTTP 403 (Forbidden) con el tipo
 * {@code .../problems/password-change-required} en la capa de infraestructura.</p>
 */
public class CambioPasswordRequeridoException extends RuntimeException {

    public CambioPasswordRequeridoException(String message) {
        super(message);
    }
}
