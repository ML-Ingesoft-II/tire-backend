package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.valueobject.CondicionClimatica;
import java.time.LocalDateTime;

/**
 * Representacion de salida del clima en ruta ({@code CondicionClimatica}).
 */
public record CondicionClimaticaDto(
        double temperaturaCelsius,
        double velocidadVientoKmh,
        String descripcion,
        LocalDateTime consultadoEn) {

    public static CondicionClimaticaDto desde(CondicionClimatica c) {
        if (c == null) {
            return null;
        }
        return new CondicionClimaticaDto(
                c.temperaturaCelsius(),
                c.velocidadVientoKmh(),
                c.descripcion(),
                null);
    }
}
