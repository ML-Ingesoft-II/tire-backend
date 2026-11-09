package com.unal.tire.domain.exception;

/**
 * Excepcion de dominio para conflictos de estado (p. ej. recurso duplicado
 * o transicion de estado invalida).
 *
 * <p>Se traduce a HTTP 409 (Conflict) en la capa de infraestructura.
 * Es una excepcion pura de dominio: no depende de ningun framework.</p>
 */
public class ConflictoEstadoException extends RuntimeException {

    public ConflictoEstadoException(String message) {
        super(message);
    }

    public ConflictoEstadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
