package com.unal.tire.application.port.in;

import java.util.UUID;

/**
 * Comando para crear un conductor.
 */
public record ComandoCrearConductor(
        String nombre,
        String documento,
        String correo,
        String telefono,
        UUID flotaId) {
}
