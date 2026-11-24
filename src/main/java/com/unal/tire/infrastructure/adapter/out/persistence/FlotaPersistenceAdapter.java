package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.application.port.out.FlotaRepositoryPort;
import com.unal.tire.domain.model.Flota;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de persistencia que implementa {@link FlotaRepositoryPort} mapeando
 * entre {@link Flota} y {@link FlotaJpaEntity}.
 */
@Component
public class FlotaPersistenceAdapter implements FlotaRepositoryPort {

    private final SpringDataFlotaRepository repository;

    public FlotaPersistenceAdapter(SpringDataFlotaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flota guardar(Flota flota) {
        return toDomain(repository.save(toEntity(flota)));
    }

    @Override
    public Optional<Flota> buscarPorId(UUID id) {
        return repository.findById(id).map(FlotaPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Flota> buscarPorNombre(String nombre) {
        return repository.findByNombre(nombre).map(FlotaPersistenceAdapter::toDomain);
    }

    @Override
    public long contar() {
        return repository.count();
    }

    private static FlotaJpaEntity toEntity(Flota f) {
        return new FlotaJpaEntity(f.getId(), f.getNombre());
    }

    private static Flota toDomain(FlotaJpaEntity e) {
        return new Flota(e.getId(), e.getNombre());
    }
}
