package com.unal.tire.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@link ConductorJpaEntity}.
 */
@Repository
public interface SpringDataConductorRepository extends JpaRepository<ConductorJpaEntity, UUID> {

    List<ConductorJpaEntity> findByFlotaId(UUID flotaId);

    boolean existsByDocumento(String documento);

    boolean existsByCorreo(String correo);
}
