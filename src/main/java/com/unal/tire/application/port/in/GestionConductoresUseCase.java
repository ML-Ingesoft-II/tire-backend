package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.PerfilConductor;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada del caso de uso de gestion de conductores de la flota.
 *
 * <p>Todas las operaciones se limitan a la flota del despachador
 * ({@code flotaId}); un recurso de otra flota responde 404.</p>
 */
public interface GestionConductoresUseCase {

    ConductorVista crear(ComandoCrearConductor comando);

    PaginaConductores listar(UUID flotaId, String q, Boolean activo, int page, int size);

    ConductorVista obtenerPorId(UUID flotaId, UUID conductorId);

    ConductorVista actualizar(UUID flotaId, UUID conductorId, ComandoActualizarConductor comando);

    ConductorVista desactivar(UUID flotaId, UUID conductorId);

    /**
     * Vista de un conductor con los datos derivados que el contrato expone:
     * el {@code correo} (del Usuario) y si tiene un viaje en curso.
     */
    record ConductorVista(PerfilConductor perfil, String correo, boolean tieneViajeEnCurso) {
    }

    /** Pagina de conductores con la metadata de paginacion del contrato. */
    record PaginaConductores(
            int pagina,
            int tamano,
            long totalElementos,
            int totalPaginas,
            List<ConductorVista> contenido) {
    }
}
