package com.unal.tire.domain.model;

import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.valueobject.TipoEvento;
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
    private UUID eventoId;
    private TipoEvento tipoEvento;
    private Double duracionSegundos;
    private LocalDateTime fechaEvento;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaAtencion;
    private Ubicacion ultimaUbicacion;
    private boolean atendida;
    private UUID atendidaPorId;
    private String atendidaPorNombre;
    private String notasDespachador;

    public AlertaCritica() {
    }

    public AlertaCritica(UUID id, UUID viajeId, UUID conductorId, UUID flotaId,
                         UUID eventoId, TipoEvento tipoEvento, Double duracionSegundos,
                         LocalDateTime fechaEvento, LocalDateTime fechaApertura,
                         LocalDateTime fechaAtencion, Ubicacion ultimaUbicacion, boolean atendida,
                         UUID atendidaPorId, String atendidaPorNombre, String notasDespachador) {
        this.id = id;
        this.viajeId = viajeId;
        this.conductorId = conductorId;
        this.flotaId = flotaId;
        this.eventoId = eventoId;
        this.tipoEvento = tipoEvento;
        this.duracionSegundos = duracionSegundos;
        this.fechaEvento = fechaEvento;
        this.fechaApertura = fechaApertura;
        this.fechaAtencion = fechaAtencion;
        this.ultimaUbicacion = ultimaUbicacion;
        this.atendida = atendida;
        this.atendidaPorId = atendidaPorId;
        this.atendidaPorNombre = atendidaPorNombre;
        this.notasDespachador = notasDespachador;
    }

    /**
     * Marca la alerta como atendida por el despachador.
     *
     * @param atendidaPorId    identificador del despachador que atiende.
     * @param atendidaPorNombre nombre del despachador que atiende.
     * @param notas            observaciones del despachador (opcional).
     * @throws ConflictoEstadoException si la alerta ya estaba atendida.
     */
    public void atender(UUID atendidaPorId, String atendidaPorNombre, String notas) {
        if (atendida) {
            throw new ConflictoEstadoException("La alerta critica ya fue atendida.");
        }
        this.atendida = true;
        this.fechaAtencion = LocalDateTime.now();
        this.atendidaPorId = atendidaPorId;
        this.atendidaPorNombre = atendidaPorNombre;
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

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Double getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(Double duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
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

    public UUID getAtendidaPorId() {
        return atendidaPorId;
    }

    public void setAtendidaPorId(UUID atendidaPorId) {
        this.atendidaPorId = atendidaPorId;
    }

    public String getAtendidaPorNombre() {
        return atendidaPorNombre;
    }

    public void setAtendidaPorNombre(String atendidaPorNombre) {
        this.atendidaPorNombre = atendidaPorNombre;
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
