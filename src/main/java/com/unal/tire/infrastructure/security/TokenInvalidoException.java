package com.unal.tire.infrastructure.security;

/**
 * Excepcion de infraestructura que indica que un token JWT es invalido,
 * esta mal formado o ha expirado.
 */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
