package com.unal.tire.application.port.in;

/**
 * Comando para registrar un evento de fatiga (microsueno) en un viaje.
 * Las coordenadas son opcionales (pueden ser null si no hay senal GPS).
 */
public record ComandoEventoFatiga(
        Double latitud,
        Double longitud,
        double duracionOjosCerradosSegundos,
        boolean confirmadoPorConductor) {
}
