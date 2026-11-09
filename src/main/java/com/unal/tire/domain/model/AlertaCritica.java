package com.unal.tire.domain.model;

import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa una alerta critica abierta para que el
 * despachador la atienda.
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class AlertaCritica {

    private UUID id;
    private UUID viajeId;
    private UUID conductorId;
    private UUID flotaId;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaAtencion;
    private Ubicacion ultimaUbicacion;
    private boolean atendida;
    private String notasDespachador;

    public AlertaCritica() {
    }

    public AlertaCritica(UUID id, UUID viajeId, UUID conductorId, UUID flotaId,
                         LocalDateTime fechaApertura, LocalDateTime fechaAtencion,
                         Ubicacion ultimaUbicacion, boolean atendida, String notasDespachador) {
        this.id = id;
        this.viajeId = viajeId;
        this.conductorId = conductorId;
        this.flotaId = flotaId;
        this.fechaApertura = fechaApertura;
        this.fechaAtencion = fechaAtencion;
        this.ultimaUbicacion = ultimaUbicacion;
        this.atendida = atendida;
        this.notasDespachador = notasDespachador;
    }

    /**
     * Marca la alerta como atendida por el despachador.
     *
     * @param notas observaciones del despachador.
     * @throws ConflictoEstadoException si la alerta ya estaba atendida.
     */
    public void atender(String notas) {
        if (atendida) {
            throw new ConflictoEstadoException("La alerta critica ya fue atendida.");
        }
        this.atendida = true;
        this.fechaAtencion = LocalDateTime.now();
        this.notasDespachador = notas;
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

    public UUID getConductorId() {
        return conductorId;
    }

    public void setConductorId(UUID conductorId) {
        this.conductorId = conductorId;
    }

    public UUID getFlotaId() {
        return flotaId;
    }

    public void setFlotaId(UUID flotaId) {
        this.flotaId = flotaId;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public Ubicacion getUltimaUbicacion() {
        return ultimaUbicacion;
    }

    public void setUltimaUbicacion(Ubicacion ultimaUbicacion) {
        this.ultimaUbicacion = ultimaUbicacion;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }

    public String getNotasDespachador() {
        return notasDespachador;
    }

    public void setNotasDespachador(String notasDespachador) {
        this.notasDespachador = notasDespachador;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertaCritica that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
