package com.unal.tire.domain.valueobject;

/**
 * Estado que el panel del despachador muestra por conductor (HU-23).
 *
 * <p>Los tres primeros corresponden al {@link NivelAlerta} del viaje en curso.
 * SIN_SENAL: el conductor esta en viaje pero no reporta telemetria hace mas de
 * 2 minutos. SIN_VIAJE: conductor activo que no tiene un viaje en curso.</p>
 */
public enum EstadoMonitoreo {
    NORMAL,
    PRECAUCION,
    CRITICO,
    SIN_SENAL,
    SIN_VIAJE
}
