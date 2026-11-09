package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.CondicionClimaticaEmbeddable;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.UbicacionEmbeddable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla {@code viajes} (raiz del agregado).
 */
@Entity
@Table(name = "viajes")
public class ViajeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "conductor_id", nullable = false)
    private UUID conductorId;

    @Column(name = "origen")
    private String origen;

    @Column(name = "destino")
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoViaje estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_alerta", nullable = false, length = 20)
    private NivelAlerta nivelAlerta;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Embedded
    private UbicacionEmbeddable ultimaUbicacion;

    @Embedded
    private CondicionClimaticaEmbeddable ultimoClima;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "viaje_fk")
    private List<EventoFatigaJpaEntity> eventosFatiga = new ArrayList<>();

    protected ViajeJpaEntity() {
    }

    public ViajeJpaEntity(UUID id, UUID conductorId, String origen, String destino,
                          EstadoViaje estado, NivelAlerta nivelAlerta, LocalDateTime fechaInicio,
                          LocalDateTime fechaFin, UbicacionEmbeddable ultimaUbicacion,
                          CondicionClimaticaEmbeddable ultimoClima,
                          List<EventoFatigaJpaEntity> eventosFatiga) {
        this.id = id;
        this.conductorId = conductorId;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
        this.nivelAlerta = nivelAlerta;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.ultimaUbicacion = ultimaUbicacion;
        this.ultimoClima = ultimoClima;
        this.eventosFatiga = eventosFatiga != null ? eventosFatiga : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getConductorId() {
        return conductorId;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public NivelAlerta getNivelAlerta() {
        return nivelAlerta;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public UbicacionEmbeddable getUltimaUbicacion() {
        return ultimaUbicacion;
    }

    public CondicionClimaticaEmbeddable getUltimoClima() {
        return ultimoClima;
    }

    public List<EventoFatigaJpaEntity> getEventosFatiga() {
        return eventosFatiga;
    }
}
