package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.infrastructure.adapter.out.persistence.embeddable.UbicacionEmbeddable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de persistencia para {@link AlertaCritica}.
 */
@Component
public class AlertaCriticaPersistenceAdapter implements AlertaCriticaRepositoryPort {

    private final SpringDataAlertaCriticaRepository repository;

    public AlertaCriticaPersistenceAdapter(SpringDataAlertaCriticaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AlertaCritica guardar(AlertaCritica alerta) {
        return toDomain(repository.save(toEntity(alerta)));
    }

    @Override
    public Optional<AlertaCritica> buscarPorId(UUID id) {
        return repository.findById(id).map(AlertaCriticaPersistenceAdapter::toDomain);
    }

    @Override
    public List<AlertaCritica> listarPorAtendida(boolean atendida) {
        return repository.findByAtendida(atendida).stream()
                .map(AlertaCriticaPersistenceAdapter::toDomain)
                .toList();
    }

    private static AlertaCriticaJpaEntity toEntity(AlertaCritica a) {
        return new AlertaCriticaJpaEntity(
                a.getId(),
                a.getViajeId(),
                a.getConductorId(),
                a.getFlotaId(),
                a.getFechaApertura(),
                a.getFechaAtencion(),
                UbicacionEmbeddable.desdeDominio(a.getUltimaUbicacion()),
                a.isAtendida(),
                a.getNotasDespachador());
    }

    private static AlertaCritica toDomain(AlertaCriticaJpaEntity e) {
        return new AlertaCritica(
                e.getId(),
                e.getViajeId(),
                e.getConductorId(),
                e.getFlotaId(),
                e.getFechaApertura(),
                e.getFechaAtencion(),
                e.getUltimaUbicacion() != null ? e.getUltimaUbicacion().aDominio() : null,
                e.isAtendida(),
                e.getNotasDespachador());
    }
}
