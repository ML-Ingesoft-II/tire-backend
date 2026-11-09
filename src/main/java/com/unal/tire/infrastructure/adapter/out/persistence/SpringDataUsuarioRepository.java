package com.unal.tire.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@link UsuarioJpaEntity}.
 */
@Repository
public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    Optional<UsuarioJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
