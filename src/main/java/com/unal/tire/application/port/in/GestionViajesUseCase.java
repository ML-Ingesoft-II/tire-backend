package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.Viaje;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada del caso de uso de gestion de viajes, telemetria y alertas.
 */
public interface GestionViajesUseCase {

    Viaje iniciarViaje(ComandoIniciarViaje comando);

    List<Viaje> listarViajes();

    Viaje obtenerViajePorId(UUID id);

    Viaje finalizarViaje(UUID id);

    void cancelarViaje(UUID id);

    Viaje registrarTelemetria(UUID viajeId, ComandoTelemetria comando);

    Viaje registrarEventoFatiga(UUID viajeId, ComandoEventoFatiga comando);

    Viaje confirmarAlerta(UUID viajeId);
}
