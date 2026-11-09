package com.unal.tire.domain.valueobject;

import com.unal.tire.domain.exception.ReglaNegocioException;

/**
 * Objeto de valor inmutable que representa una coordenada geografica.
 *
 * @param latitud  grados decimales, valido entre -90 y 90.
 * @param longitud grados decimales, valido entre -180 y 180.
 */
public record Ubicacion(double latitud, double longitud) {

    public Ubicacion {
        if (latitud < -90 || latitud > 90) {
            throw new ReglaNegocioException(
                    "La latitud debe estar entre -90 y 90 grados.");
        }
        if (longitud < -180 || longitud > 180) {
            throw new ReglaNegocioException(
                    "La longitud debe estar entre -180 y 180 grados.");
        }
    }
}
