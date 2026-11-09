package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.CondicionClimaticaEmbeddable;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.UbicacionEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla {@code eventos_fatiga}. Es hija del agregado
 * {@link ViajeJpaEntity}.
 */
@Entity
@Table(name = "eventos_fatiga")
public class EventoFatigaJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "viaje_id", nullable = false)
    private UUID viajeId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Embedded
    private UbicacionEmbeddable ubicacion;

    @Column(name = "duracion_ojos_cerrados_segundos", nullable = false)
    private double duracionOjosCerradosSegundos;

    @Column(name = "confirmado_por_conductor", nullable = false)
    private boolean confirmadoPorConductor;

    @Embedded
    private CondicionClimaticaEmbeddable climaEnRuta;

    protected EventoFatigaJpaEntity() {
    }

    public EventoFatigaJpaEntity(UUID id, UUID viajeId, LocalDateTime fechaHora,
                                 UbicacionEmbeddable ubicacion, double duracionOjosCerradosSegundos,
                                 boolean confirmadoPorConductor,
                                 CondicionClimaticaEmbeddable climaEnRuta) {
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

    public UUID getViajeId() {
        return viajeId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public UbicacionEmbeddable getUbicacion() {
        return ubicacion;
    }

    public double getDuracionOjosCerradosSegundos() {
        return duracionOjosCerradosSegundos;
    }

    public boolean isConfirmadoPorConductor() {
        return confirmadoPorConductor;
    }

    public CondicionClimaticaEmbeddable getClimaEnRuta() {
        return climaEnRuta;
    }
}
