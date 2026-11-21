package com.unal.tire.application.port.in;

import com.unal.tire.domain.valueobject.TipoDocumento;

/**
 * Comando para actualizar parcialmente (PATCH) un conductor. Los campos nulos
 * se ignoran (solo cambian los enviados). {@code activo=true} reactiva.
 */
public record ComandoActualizarConductor(
        String nombres,
        String apellidos,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        String placaVehiculo,
        Boolean activo) {
}
