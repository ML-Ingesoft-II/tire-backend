package com.unal.tire.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@link FlotaJpaEntity}.
 */
@Repository
public interface SpringDataFlotaRepository extends JpaRepository<FlotaJpaEntity, UUID> {

    Optional<FlotaJpaEntity> findByNombre(String nombre);
}
