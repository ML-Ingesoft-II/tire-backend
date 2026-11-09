package com.unal.tire.infrastructure.adapter.out.persistence.embeddable;

import com.unal.tire.domain.valueobject.Ubicacion;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embebible JPA para el value object {@link Ubicacion}.
 */
@Embeddable
public class UbicacionEmbeddable {

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    protected UbicacionEmbeddable() {
    }

    public UbicacionEmbeddable(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public static UbicacionEmbeddable desdeDominio(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return null;
        }
        return new UbicacionEmbeddable(ubicacion.latitud(), ubicacion.longitud());
    }

    public Ubicacion aDominio() {
        if (latitud == null || longitud == null) {
            return null;
        }
        return new Ubicacion(latitud, longitud);
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
}
