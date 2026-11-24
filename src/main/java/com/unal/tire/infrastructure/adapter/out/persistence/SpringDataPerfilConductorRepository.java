package com.unal.tire.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@link PerfilConductorJpaEntity}.
 */
@Repository
public interface SpringDataPerfilConductorRepository
        extends JpaRepository<PerfilConductorJpaEntity, UUID> {

    List<PerfilConductorJpaEntity> findByFlotaIdOrderByNombresAsc(UUID flotaId);

    List<PerfilConductorJpaEntity> findByFlotaIdAndActivoTrueOrderByNombresAsc(UUID flotaId);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
