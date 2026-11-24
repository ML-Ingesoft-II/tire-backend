package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.domain.model.PerfilConductor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de persistencia que implementa {@link ConductorRepositoryPort}
 * mapeando entre {@link PerfilConductor} y {@link PerfilConductorJpaEntity}.
 *
 * <p>El correo de un conductor vive en su {@code Usuario}; por eso la
 * verificacion de correo duplicado se apoya en el repositorio de usuarios.</p>
 */
@Component
public class PerfilConductorPersistenceAdapter implements ConductorRepositoryPort {

    private final SpringDataPerfilConductorRepository perfilRepository;
    private final SpringDataUsuarioRepository usuarioRepository;

    public PerfilConductorPersistenceAdapter(
            SpringDataPerfilConductorRepository perfilRepository,
            SpringDataUsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public PerfilConductor guardar(PerfilConductor perfil) {
        return toDomain(perfilRepository.save(toEntity(perfil)));
    }

    @Override
    public Optional<PerfilConductor> buscarPorId(UUID usuarioId) {
        return perfilRepository.findById(usuarioId)
                .map(PerfilConductorPersistenceAdapter::toDomain);
    }

    @Override
    public List<PerfilConductor> listarPorFlota(UUID flotaId) {
        return perfilRepository.findByFlotaIdOrderByNombresAsc(flotaId).stream()
                .map(PerfilConductorPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<PerfilConductor> listarActivosPorFlota(UUID flotaId) {
        return perfilRepository.findByFlotaIdAndActivoTrueOrderByNombresAsc(flotaId).stream()
                .map(PerfilConductorPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existePorDocumento(String numeroDocumento) {
        return perfilRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public boolean existePorCorreoEnFlota(String correo, UUID flotaId) {
        // El correo es unico globalmente a nivel de usuario; basta con comprobar
        // su existencia en la tabla de usuarios.
        return usuarioRepository.existsByEmail(correo);
    }

    private static PerfilConductorJpaEntity toEntity(PerfilConductor p) {
        return new PerfilConductorJpaEntity(
                p.getUsuarioId(),
                p.getNombres(),
                p.getApellidos(),
                p.getTipoDocumento(),
                p.getNumeroDocumento(),
                p.getTelefono(),
                p.getPlacaVehiculo(),
                p.getFlotaId(),
                p.isActivo(),
                p.getCreadoEn());
    }

    private static PerfilConductor toDomain(PerfilConductorJpaEntity e) {
        return new PerfilConductor(
                e.getUsuarioId(),
                e.getNombres(),
                e.getApellidos(),
                e.getTipoDocumento(),
                e.getNumeroDocumento(),
                e.getTelefono(),
                e.getPlacaVehiculo(),
                e.getFlotaId(),
                e.isActivo(),
                e.getCreadoEn());
    }
}
