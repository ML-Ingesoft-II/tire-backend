package com.unal.tire.domain.exception;

/**
 * Excepcion de dominio para accesos denegados por reglas de negocio
 * (p. ej. un CONDUCTOR intentando operar recursos de otro conductor).
 *
 * <p>Se traduce a HTTP 403 (Forbidden) en la capa de infraestructura.
 * Es una excepcion pura de dominio: no depende de ningun framework.</p>
 */
public class AccesoDenegadoDominioException extends RuntimeException {

    public AccesoDenegadoDominioException(String message) {
        super(message);
    }

    public AccesoDenegadoDominioException(String message, Throwable cause) {
        super(message, cause);
    }
}
