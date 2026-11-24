package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.CondicionClimaticaEmbeddable;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.UbicacionEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEvento tipoEvento;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Embedded
    private UbicacionEmbeddable ubicacion;

    @Column(name = "duracion_segundos", nullable = false)
    private double duracionSegundos;

    @Column(name = "respondio")
    private Boolean respondio;

    @Column(name = "tiempo_respuesta_segundos")
    private Double tiempoRespuestaSegundos;

    @Embedded
    private CondicionClimaticaEmbeddable climaEnRuta;

    protected EventoFatigaJpaEntity() {
    }

    public EventoFatigaJpaEntity(UUID id, UUID viajeId, TipoEvento tipoEvento, LocalDateTime fechaHora,
                                 UbicacionEmbeddable ubicacion, double duracionSegundos,
                                 Boolean respondio, Double tiempoRespuestaSegundos,
                                 CondicionClimaticaEmbeddable climaEnRuta) {
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

    public UUID getId() {
        return id;
    }

    public UUID getViajeId() {
        return viajeId;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public UbicacionEmbeddable getUbicacion() {
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

    public CondicionClimaticaEmbeddable getClimaEnRuta() {
        return climaEnRuta;
    }
}
