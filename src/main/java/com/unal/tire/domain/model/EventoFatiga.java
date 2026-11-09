package com.unal.tire.domain.model;

import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un evento de fatiga (microsueno)
 * detectado durante un viaje.
 *
 * <p>Invariante de dominio: la duracion de ojos cerrados debe superar el
 * umbral de microsueno del MVP (1.5 s) y la fecha/hora no puede ser nula.</p>
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class EventoFatiga {

    /** Umbral minimo (segundos) de ojos cerrados para considerarse microsueno. */
    public static final double UMBRAL_MICROSUENO_SEGUNDOS = 1.5;

    private UUID id;
    private UUID viajeId;
    private LocalDateTime fechaHora;
    private Ubicacion ubicacion;
    private double duracionOjosCerradosSegundos;
    private boolean confirmadoPorConductor;
    private CondicionClimatica climaEnRuta;

    public EventoFatiga(UUID id, UUID viajeId, LocalDateTime fechaHora, Ubicacion ubicacion,
                        double duracionOjosCerradosSegundos, boolean confirmadoPorConductor,
                        CondicionClimatica climaEnRuta) {
        if (fechaHora == null) {
            throw new ReglaNegocioException("La fecha/hora del evento de fatiga es obligatoria.");
        }
        if (duracionOjosCerradosSegundos < UMBRAL_MICROSUENO_SEGUNDOS) {
            throw new ReglaNegocioException(
                    "La duracion de ojos cerrados debe ser de al menos "
                            + UMBRAL_MICROSUENO_SEGUNDOS + " segundos para considerarse microsueno.");
        }
        this.id = id;
        this.viajeId = viajeId;
        this.fechaHora = fechaHora;
        this.ubicacion = ubicacion;
        this.duracionOjosCerradosSegundos = duracionOjosCerradosSegundos;
        this.confirmadoPorConductor = confirmadoPorConductor;
        this.climaEnRuta = climaEnRuta;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getViajeId() {
        return viajeId;
    }

    public void setViajeId(UUID viajeId) {
        this.viajeId = viajeId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public double getDuracionOjosCerradosSegundos() {
        return duracionOjosCerradosSegundos;
    }

    public boolean isConfirmadoPorConductor() {
        return confirmadoPorConductor;
    }

    public CondicionClimatica getClimaEnRuta() {
        return climaEnRuta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoFatiga that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
