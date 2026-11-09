package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.Conductor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de conductores.
 */
public interface ConductorRepositoryPort {

    Conductor guardar(Conductor conductor);

    Optional<Conductor> buscarPorId(UUID id);

    List<Conductor> listarTodos();

    List<Conductor> listarPorFlota(UUID flotaId);

    boolean existePorDocumento(String documento);

    boolean existePorCorreo(String correo);
}
