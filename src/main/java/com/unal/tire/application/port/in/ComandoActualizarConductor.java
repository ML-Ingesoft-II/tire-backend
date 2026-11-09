package com.unal.tire.application.port.in;

/**
 * Comando para actualizar parcialmente (PATCH) un conductor. Los campos nulos
 * se ignoran.
 */
public record ComandoActualizarConductor(
        String nombre,
        String telefono,
        Boolean activo) {
}
