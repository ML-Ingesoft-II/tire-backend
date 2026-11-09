package com.unal.tire.domain.exception;

/**
 * Excepcion de dominio para violaciones de reglas de negocio.
 *
 * <p>Se traduce a HTTP 400 (Bad Request) en la capa de infraestructura.
 * Es una excepcion pura de dominio: no depende de ningun framework.</p>
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String message) {
        super(message);
    }

    public ReglaNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
