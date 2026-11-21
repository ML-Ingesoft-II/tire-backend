package com.unal.tire.application.port.in;

import com.unal.tire.domain.valueobject.TipoDocumento;
import java.util.UUID;

/**
 * Comando para crear un conductor de flota. La {@code flotaId} se toma del token
 * del despachador. Crea el {@code Usuario} (rol CONDUCTOR) y el
 * {@code PerfilConductor} con el mismo UUID.
 */
public record ComandoCrearConductor(
        String nombres,
        String apellidos,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        String placaVehiculo,
        UUID flotaId) {
}
