package com.unal.tire.infrastructure.adapter.out.persistence.embeddable;

import com.unal.tire.domain.valueobject.CondicionClimatica;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embebible JPA para el value object {@link CondicionClimatica}.
 */
@Embeddable
public class CondicionClimaticaEmbeddable {

    @Column(name = "clima_temperatura_celsius")
    private Double temperaturaCelsius;

    @Column(name = "clima_viento_kmh")
    private Double velocidadVientoKmh;

    @Column(name = "clima_descripcion")
    private String descripcion;

    protected CondicionClimaticaEmbeddable() {
    }

    public CondicionClimaticaEmbeddable(Double temperaturaCelsius, Double velocidadVientoKmh,
                                        String descripcion) {
        this.temperaturaCelsius = temperaturaCelsius;
        this.velocidadVientoKmh = velocidadVientoKmh;
        this.descripcion = descripcion;
    }

    public static CondicionClimaticaEmbeddable desdeDominio(CondicionClimatica clima) {
        if (clima == null) {
            return null;
        }
        return new CondicionClimaticaEmbeddable(
                clima.temperaturaCelsius(), clima.velocidadVientoKmh(), clima.descripcion());
    }

    public CondicionClimatica aDominio() {
        if (temperaturaCelsius == null || velocidadVientoKmh == null) {
            return null;
        }
        return new CondicionClimatica(temperaturaCelsius, velocidadVientoKmh, descripcion);
    }

    public Double getTemperaturaCelsius() {
        return temperaturaCelsius;
    }

    public void setTemperaturaCelsius(Double temperaturaCelsius) {
        this.temperaturaCelsius = temperaturaCelsius;
    }

    public Double getVelocidadVientoKmh() {
        return velocidadVientoKmh;
    }

    public void setVelocidadVientoKmh(Double velocidadVientoKmh) {
        this.velocidadVientoKmh = velocidadVientoKmh;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
