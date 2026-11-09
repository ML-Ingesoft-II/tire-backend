package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.CondicionClimaticaEmbeddable;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.UbicacionEmbeddable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de persistencia para el agregado {@link Viaje}, incluyendo sus
 * eventos de fatiga.
 */
@Component
public class ViajePersistenceAdapter implements ViajeRepositoryPort {

    private final SpringDataViajeRepository repository;

    public ViajePersistenceAdapter(SpringDataViajeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Viaje guardar(Viaje viaje) {
        return toDomain(repository.save(toEntity(viaje)));
    }

    @Override
    public Optional<Viaje> buscarPorId(UUID id) {
        return repository.findById(id).map(ViajePersistenceAdapter::toDomain);
    }

    @Override
    public List<Viaje> listarTodos() {
        return repository.findAll().stream().map(ViajePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Viaje> listarPorEstado(EstadoViaje estado) {
        return repository.findByEstado(estado).stream()
                .map(ViajePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existeViajeEnCursoParaConductor(UUID conductorId) {
        return repository.existsByConductorIdAndEstado(conductorId, EstadoViaje.EN_CURSO);
    }

    private static ViajeJpaEntity toEntity(Viaje v) {
        List<EventoFatigaJpaEntity> eventos = v.getEventosFatiga().stream()
                .map(ViajePersistenceAdapter::toEventoEntity)
                .toList();

        return new ViajeJpaEntity(
                v.getId(),
                v.getConductorId(),
                v.getOrigen(),
                v.getDestino(),
                v.getEstado(),
                v.getNivelAlerta(),
                v.getFechaInicio(),
                v.getFechaFin(),
                UbicacionEmbeddable.desdeDominio(v.getUltimaUbicacion()),
                CondicionClimaticaEmbeddable.desdeDominio(v.getUltimoClima()),
                new java.util.ArrayList<>(eventos));
    }

    private static EventoFatigaJpaEntity toEventoEntity(EventoFatiga e) {
        return new EventoFatigaJpaEntity(
                e.getId(),
                e.getViajeId(),
                e.getFechaHora(),
                UbicacionEmbeddable.desdeDominio(e.getUbicacion()),
                e.getDuracionOjosCerradosSegundos(),
                e.isConfirmadoPorConductor(),
                CondicionClimaticaEmbeddable.desdeDominio(e.getClimaEnRuta()));
    }

    private static Viaje toDomain(ViajeJpaEntity e) {
        List<EventoFatiga> eventos = e.getEventosFatiga().stream()
                .map(ViajePersistenceAdapter::toEventoDomain)
                .toList();

        return new Viaje(
                e.getId(),
                e.getConductorId(),
                e.getOrigen(),
                e.getDestino(),
                e.getEstado(),
                e.getNivelAlerta(),
                e.getFechaInicio(),
                e.getFechaFin(),
                e.getUltimaUbicacion() != null ? e.getUltimaUbicacion().aDominio() : null,
                e.getUltimoClima() != null ? e.getUltimoClima().aDominio() : null,
                eventos);
    }

    private static EventoFatiga toEventoDomain(EventoFatigaJpaEntity e) {
        return new EventoFatiga(
                e.getId(),
                e.getViajeId(),
                e.getFechaHora(),
                e.getUbicacion() != null ? e.getUbicacion().aDominio() : null,
                e.getDuracionOjosCerradosSegundos(),
                e.isConfirmadoPorConductor(),
                e.getClimaEnRuta() != null ? e.getClimaEnRuta().aDominio() : null);
    }
}
