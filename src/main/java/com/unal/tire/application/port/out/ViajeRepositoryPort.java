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

    boolean existeViajeEnCursoParaConductor(UUID conductorId);
}
