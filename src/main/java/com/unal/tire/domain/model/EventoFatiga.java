package com.unal.tire.domain.model;

import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un evento de fatiga detectado durante un
 * viaje.
 *
 * <p>Dos tipos (ver {@link TipoEvento}):</p>
 * <ul>
 *   <li>{@code MICROSUENO}: cierre de ojos por un tiempo igual o superior al
 *       umbral de 1,5 s. Lleva {@code respondio} (si el conductor toco "Estoy
 *       bien" en la ventana de 5 s) y, en ese caso, {@code tiempoRespuestaSegundos}.</li>
 *   <li>{@code ROSTRO_NO_VISIBLE}: el rostro dejo de verse por mas de 5 s. No
 *       cambia el nivel de alerta.</li>
 * </ul>
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class EventoFatiga {

    /** Umbral minimo (segundos) de ojos cerrados para considerarse microsueno. */
    public static final double UMBRAL_MICROSUENO_SEGUNDOS = 1.5;

    /** Umbral minimo (segundos) sin ver el rostro para registrarse el evento. */
    public static final double UMBRAL_ROSTRO_NO_VISIBLE_SEGUNDOS = 5.0;

    private UUID id;
    private UUID viajeId;
    private TipoEvento tipoEvento;
    private LocalDateTime fechaHora;
    private Ubicacion ubicacion;
    private double duracionSegundos;
    private Boolean respondio;
    private Double tiempoRespuestaSegundos;
    private CondicionClimatica climaEnRuta;

    public EventoFatiga(UUID id, UUID viajeId, TipoEvento tipoEvento, LocalDateTime fechaHora,
                        Ubicacion ubicacion, double duracionSegundos, Boolean respondio,
                        Double tiempoRespuestaSegundos, CondicionClimatica climaEnRuta) {
        if (tipoEvento == null) {
            throw new ReglaNegocioException("El tipo de evento es obligatorio.");
        }
        if (fechaHora == null) {
            throw new ReglaNegocioException("La fecha/hora del evento de fatiga es obligatoria.");
        }
        if (tipoEvento == TipoEvento.MICROSUENO) {
            if (duracionSegundos < UMBRAL_MICROSUENO_SEGUNDOS) {
                throw new ReglaNegocioException(
                        "La duracion de ojos cerrados debe ser de al menos "
                                + UMBRAL_MICROSUENO_SEGUNDOS + " segundos para considerarse microsueno.");
            }
            if (respondio == null) {
                throw new ReglaNegocioException(
                        "Un microsueno debe indicar si el conductor respondio.");
            }
        } else if (tipoEvento == TipoEvento.ROSTRO_NO_VISIBLE
                && duracionSegundos < UMBRAL_ROSTRO_NO_VISIBLE_SEGUNDOS) {
            throw new ReglaNegocioException(
                    "El evento de rostro no visible requiere al menos "
                            + UMBRAL_ROSTRO_NO_VISIBLE_SEGUNDOS + " segundos.");
        }

        this.id = id;
        this.viajeId = viajeId;
        this.tipoEvento = tipoEvento;
        this.fechaHora = fechaHora;
        this.ubicacion = ubicacion;
        this.duracionSegundos = duracionSegundos;
        this.respondio = respondio;
        this.tiempoRespuestaSegundos = tiempoRespuestaSegundos;
        this.climaEnRuta = climaEnRuta;
    }

    /**
     * @return true si el evento es un microsueno que el conductor confirmo
     *     con "Estoy bien". Usado por el patron State para recalcular el nivel.
     */
    public boolean fueConfirmado() {
        return tipoEvento == TipoEvento.MICROSUENO && Boolean.TRUE.equals(respondio);
    }

    /**
     * @return true si el evento afecta el nivel de alerta (solo los microsuenos).
     */
    public boolean afectaNivelAlerta() {
        return tipoEvento == TipoEvento.MICROSUENO;
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

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public double getDuracionSegundos() {
        return duracionSegundos;
    }

    public Boolean getRespondio() {
        return respondio;
    }

    public Double getTiempoRespuestaSegundos() {
        return tiempoRespuestaSegundos;
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
