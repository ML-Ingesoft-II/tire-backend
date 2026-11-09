package com.unal.tire.domain.valueobject;

/**
 * Objeto de valor inmutable que describe las condiciones climaticas en ruta.
 *
 * @param temperaturaCelsius   temperatura ambiente en grados Celsius.
 * @param velocidadVientoKmh   velocidad del viento en kilometros por hora.
 * @param descripcion          descripcion textual del clima (p. ej. "lluvia").
 */
public record CondicionClimatica(
        double temperaturaCelsius,
        double velocidadVientoKmh,
        String descripcion) {
}
