package com.unal.tire.domain.valueobject;

/**
 * Tipo de evento de fatiga detectado durante un viaje.
 *
 * <p>MICROSUENO: cierre de ojos por un tiempo igual o superior al umbral.
 * ROSTRO_NO_VISIBLE: el rostro dejo de verse en la camara por mas de 5 s.</p>
 */
public enum TipoEvento {
    MICROSUENO,
    ROSTRO_NO_VISIBLE
}
