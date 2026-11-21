package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoViaje;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de viajes (agregado raiz).
 */
public interface ViajeRepositoryPort {

    Viaje guardar(Viaje viaje);

    Optional<Viaje> buscarPorId(UUID id);

    List<Viaje> listarTodos();

    List<Viaje> listarPorEstado(EstadoViaje estado);

    /** Viajes de un conductor, mas reciente primero. */
    List<Viaje> listarPorConductor(UUID conductorId);

    /** Viajes de los conductores indicados (una flota), mas reciente primero. */
    List<Viaje> listarPorConductores(java.util.Collection<UUID> conductorIds);

    boolean existeViajeEnCursoParaConductor(UUID conductorId);

    /** Viaje en curso de un conductor, si lo tiene. */
    Optional<Viaje> buscarEnCursoDeConductor(UUID conductorId);
}
