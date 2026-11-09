package com.unal.tire.application.port.in;

/**
 * Comando para registrar telemetria de posicion de un viaje en curso.
 */
public record ComandoTelemetria(
        double latitud,
        double longitud) {
}
