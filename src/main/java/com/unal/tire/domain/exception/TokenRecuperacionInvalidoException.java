package com.unal.tire.domain.exception;

/**
 * Excepcion de dominio para un token de recuperacion de contrasena vencido o
 * ya usado (HU-04).
 *
 * <p>Se traduce a HTTP 410 (Gone) con el tipo {@code .../problems/token-expired}
 * en la capa de infraestructura.</p>
 */
public class TokenRecuperacionInvalidoException extends RuntimeException {

    public TokenRecuperacionInvalidoException(String message) {
        super(message);
    }
}
