package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.PerfilConductor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de perfiles de conductor.
 *
 * <p>El identificador de un {@link PerfilConductor} ({@code usuarioId}) coincide
 * con el id del {@link com.unal.tire.domain.model.Usuario} con rol CONDUCTOR.</p>
 */
public interface ConductorRepositoryPort {

    PerfilConductor guardar(PerfilConductor perfil);

    Optional<PerfilConductor> buscarPorId(UUID usuarioId);

    List<PerfilConductor> listarPorFlota(UUID flotaId);

    /** Conductores activos de una flota. */
    List<PerfilConductor> listarActivosPorFlota(UUID flotaId);

    boolean existePorDocumento(String numeroDocumento);

    boolean existePorCorreoEnFlota(String correo, UUID flotaId);
}
