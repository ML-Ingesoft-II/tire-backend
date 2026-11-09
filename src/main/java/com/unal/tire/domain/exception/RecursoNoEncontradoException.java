package com.unal.tire.domain.exception;

/**
 * Excepcion de dominio para recursos inexistentes.
 *
 * <p>Se traduce a HTTP 404 (Not Found) en la capa de infraestructura.
 * Es una excepcion pura de dominio: no depende de ningun framework.</p>
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }

    public RecursoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
