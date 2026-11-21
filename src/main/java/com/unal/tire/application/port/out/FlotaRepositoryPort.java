package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.Flota;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de flotas.
 */
public interface FlotaRepositoryPort {

    Flota guardar(Flota flota);

    Optional<Flota> buscarPorId(UUID id);

    Optional<Flota> buscarPorNombre(String nombre);

    long contar();
}
