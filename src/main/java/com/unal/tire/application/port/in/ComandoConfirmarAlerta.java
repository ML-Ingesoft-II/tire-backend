package com.unal.tire.application.port.in;

/**
 * Comando para confirmar "Estoy bien" tras una escalada a CRITICO.
 *
 * @param tiempoRespuestaSegundos segundos desde la deteccion del microsueno
 *                                hasta que el conductor toco "Estoy bien".
 */
public record ComandoConfirmarAlerta(
        double tiempoRespuestaSegundos) {
}
