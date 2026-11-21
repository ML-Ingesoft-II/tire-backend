package com.unal.tire.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa la flota de una empresa de transporte.
 *
 * <p>Agrupa a los conductores y despachadores de una misma empresa. El alcance
 * de todo lo que consulta o modifica un despachador se limita a su flota.</p>
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class Flota {

    private UUID id;
    private String nombre;

    public Flota() {
    }

    public Flota(UUID id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Flota flota)) {
            return false;
        }
        return Objects.equals(id, flota.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
