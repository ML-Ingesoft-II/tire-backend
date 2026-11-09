package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de persistencia que implementa {@link UsuarioRepositoryPort}
 * mapeando entre el modelo de dominio {@link Usuario} y la entidad JPA
 * {@link UsuarioJpaEntity}.
 */
@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;

    public UsuarioPersistenceAdapter(SpringDataUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario guardar(Usuario u) {
        UsuarioJpaEntity entidad = toEntity(u);
        return toDomain(repository.save(entidad));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(UsuarioPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return repository.findById(id).map(UsuarioPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return repository.existsByEmail(email);
    }

    private static UsuarioJpaEntity toEntity(Usuario u) {
        return new UsuarioJpaEntity(
                u.getId(),
                u.getEmail(),
                u.getPasswordHash(),
                u.getNombre(),
                u.getRol(),
                u.getFlotaId(),
                u.isActivo());
    }

    private static Usuario toDomain(UsuarioJpaEntity e) {
        return new Usuario(
                e.getId(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getNombre(),
                e.getRol(),
                e.getFlotaId(),
                e.isActivo());
    }
}
