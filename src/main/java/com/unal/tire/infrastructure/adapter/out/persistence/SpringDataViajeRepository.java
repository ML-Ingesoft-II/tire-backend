package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.domain.valueobject.EstadoViaje;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@link ViajeJpaEntity}.
 */
@Repository
public interface SpringDataViajeRepository extends JpaRepository<ViajeJpaEntity, UUID> {

    List<ViajeJpaEntity> findByEstado(EstadoViaje estado);

    boolean existsByConductorIdAndEstado(UUID conductorId, EstadoViaje estado);
}
