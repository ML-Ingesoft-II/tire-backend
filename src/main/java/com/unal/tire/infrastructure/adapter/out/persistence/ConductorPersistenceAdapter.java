package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.domain.model.Conductor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de persistencia que implementa {@link ConductorRepositoryPort}
 * mapeando entre {@link Conductor} y {@link ConductorJpaEntity}.
 */
@Component
public class ConductorPersistenceAdapter implements ConductorRepositoryPort {

    private final SpringDataConductorRepository repository;

    public ConductorPersistenceAdapter(SpringDataConductorRepository repository) {
        this.repository = repository;
    }

    @Override
    public Conductor guardar(Conductor conductor) {
        return toDomain(repository.save(toEntity(conductor)));
    }

    @Override
    public Optional<Conductor> buscarPorId(UUID id) {
        return repository.findById(id).map(ConductorPersistenceAdapter::toDomain);
    }

    @Override
    public List<Conductor> listarTodos() {
        return repository.findAll().stream().map(ConductorPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Conductor> listarPorFlota(UUID flotaId) {
        return repository.findByFlotaId(flotaId).stream()
                .map(ConductorPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return repository.existsByDocumento(documento);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repository.existsByCorreo(correo);
    }

    private static ConductorJpaEntity toEntity(Conductor c) {
        return new ConductorJpaEntity(
                c.getId(),
                c.getNombre(),
                c.getDocumento(),
                c.getCorreo(),
                c.getTelefono(),
                c.getFlotaId(),
                c.isActivo(),
                c.getCreadoEn());
    }

    private static Conductor toDomain(ConductorJpaEntity e) {
        return new Conductor(
                e.getId(),
                e.getNombre(),
                e.getDocumento(),
                e.getCorreo(),
                e.getTelefono(),
                e.getFlotaId(),
                e.isActivo(),
                e.getCreadoEn());
    }
}
