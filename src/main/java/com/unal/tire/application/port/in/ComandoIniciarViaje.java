package com.unal.tire.application.port.in;

import java.util.UUID;

/**
 * Comando para iniciar un viaje. Las coordenadas iniciales son opcionales
 * (pueden ser null si no hay senal GPS al arrancar).
 */
public record ComandoIniciarViaje(
        UUID conductorId,
        String origen,
        String destino,
        Double latitudInicial,
        Double longitudInicial) {
}
