package com.unal.tire.application.port.in;

/**
 * Comando para registrar telemetria de un viaje en curso. La velocidad es
 * opcional (puede ser null).
 */
public record ComandoTelemetria(
        double latitud,
        double longitud,
        Double velocidadKmh) {
}
