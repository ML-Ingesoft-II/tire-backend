package com.unal.tire.application.port.out;

import com.unal.tire.domain.valueobject.CondicionClimatica;

/**
 * Puerto de salida para consultar las condiciones climaticas en una ubicacion.
 *
 * <p>Abstrae la fuente externa de datos meteorologicos del nucleo de la
 * aplicacion.</p>
 */
public interface ConsultorClimaPort {

    /**
     * Consulta el clima actual en las coordenadas indicadas.
     *
     * @param latitud  latitud en grados decimales.
     * @param longitud longitud en grados decimales.
     * @return las condiciones climaticas; nunca {@code null} (devuelve un valor
     * por defecto si la fuente externa no esta disponible).
     */
    CondicionClimatica consultarClimaActual(double latitud, double longitud);
}
