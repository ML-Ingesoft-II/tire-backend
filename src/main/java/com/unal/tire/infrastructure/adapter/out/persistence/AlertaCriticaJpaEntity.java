package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.domain.valueobject.TipoEvento;
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
 * Entidad JPA que mapea la tabla {@code alertas_criticas}.
 */
@Entity
@Table(name = "alertas_criticas")
public class AlertaCriticaJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "viaje_id", nullable = false)
    private UUID viajeId;

    @Column(name = "conductor_id")
    private UUID conductorId;

    @Column(name = "flota_id")
    private UUID flotaId;

    @Column(name = "evento_id")
    private UUID eventoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 30)
    private TipoEvento tipoEvento;

    @Column(name = "duracion_segundos")
    private Double duracionSegundos;

    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @Embedded
    private UbicacionEmbeddable ultimaUbicacion;

    @Column(name = "atendida", nullable = false)
    private boolean atendida;

    @Column(name = "atendida_por_id")
    private UUID atendidaPorId;

    @Column(name = "atendida_por_nombre")
    private String atendidaPorNombre;

    @Column(name = "notas_despachador", length = 1000)
    private String notasDespachador;

    protected AlertaCriticaJpaEntity() {
    }

    public AlertaCriticaJpaEntity(UUID id, UUID viajeId, UUID conductorId, UUID flotaId,
                                  UUID eventoId, TipoEvento tipoEvento, Double duracionSegundos,
                                  LocalDateTime fechaEvento, LocalDateTime fechaApertura,
                                  LocalDateTime fechaAtencion, UbicacionEmbeddable ultimaUbicacion,
                                  boolean atendida, UUID atendidaPorId, String atendidaPorNombre,
                                  String notasDespachador) {
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

    public UUID getId() {
        return id;
    }

    public UUID getViajeId() {
        return viajeId;
    }

    public UUID getConductorId() {
        return conductorId;
    }

    public UUID getFlotaId() {
        return flotaId;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public Double getDuracionSegundos() {
        return duracionSegundos;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public UbicacionEmbeddable getUltimaUbicacion() {
        return ultimaUbicacion;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public UUID getAtendidaPorId() {
        return atendidaPorId;
    }

    public String getAtendidaPorNombre() {
        return atendidaPorNombre;
    }

    public String getNotasDespachador() {
        return notasDespachador;
    }
}
