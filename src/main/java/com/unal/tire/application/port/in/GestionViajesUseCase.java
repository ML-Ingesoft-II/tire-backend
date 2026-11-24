package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada del caso de uso de gestion de viajes, telemetria y eventos.
 *
 * <p>Alcance: un CONDUCTOR solo ve y modifica sus propios viajes; un
 * DESPACHADOR consulta los viajes de su flota. Un recurso fuera de alcance
 * responde 404.</p>
 */
public interface GestionViajesUseCase {

    Viaje iniciarViaje(ComandoIniciarViaje comando);

    PaginaViajes listarViajes(UUID usuarioId, RolUsuario rol, UUID flotaId,
                              EstadoViaje estado, int page, int size);

    Viaje obtenerViajeActivo(UUID conductorId);

    Viaje obtenerViajePorId(UUID usuarioId, RolUsuario rol, UUID flotaId, UUID id);

    Viaje finalizarViaje(UUID conductorId, UUID id);

    Viaje registrarTelemetria(UUID conductorId, UUID viajeId, ComandoTelemetria comando);

    Viaje registrarEventoFatiga(UUID conductorId, UUID viajeId, ComandoEventoFatiga comando);

    Viaje confirmarAlerta(UUID conductorId, UUID viajeId, ComandoConfirmarAlerta comando);

    /** Pagina de viajes con la metadata de paginacion del contrato. */
    record PaginaViajes(
            int pagina,
            int tamano,
            long totalElementos,
            int totalPaginas,
            List<Viaje> contenido) {
    }
}
