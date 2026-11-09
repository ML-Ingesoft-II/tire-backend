package com.unal.tire.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para {@link AlertaCriticaJpaEntity}.
 */
@Repository
public interface SpringDataAlertaCriticaRepository extends JpaRepository<AlertaCriticaJpaEntity, UUID> {

    List<AlertaCriticaJpaEntity> findByAtendida(boolean atendida);
}
