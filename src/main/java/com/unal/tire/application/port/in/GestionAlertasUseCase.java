package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import com.unal.tire.domain.valueobject.TipoEvento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de entrada para la operacion del despachador (HU-20, HU-23, HU-24):
 * estado de la flota, alertas criticas e historial de incidentes.
 *
 * <p>Todo con alcance por la flota del token.</p>
 */
public interface GestionAlertasUseCase {

    EstadoFlota obtenerEstadoFlota(UUID flotaId);

    List<AlertaVista> listarAlertasAbiertas(UUID flotaId);

    AlertaDetalleVista obtenerAlerta(UUID flotaId, UUID alertaId);

    AlertaVista atenderAlerta(UUID flotaId, UUID despachadorId, String despachadorNombre,
                              UUID alertaId, String notas);

    PaginaEventos historialEventos(UUID flotaId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                   TipoEvento tipoEvento, UUID conductorId, int page, int size);

    // ----- Vistas (datos ya resueltos para que el adaptador arme el JSON) -----

    /** Resumen de un conductor usado en flota, alertas e historial. */
    record ConductorResumen(UUID id, String nombreCompleto, String placaVehiculo, String telefono) {
        public static ConductorResumen desde(PerfilConductor p) {
            return new ConductorResumen(p.getUsuarioId(), p.getNombreCompleto(),
                    p.getPlacaVehiculo(), p.getTelefono());
        }
    }

    record ConductorEnViaje(
            ConductorResumen conductor,
            UUID viajeId,
            EstadoMonitoreo estado,
            com.unal.tire.domain.valueobject.NivelAlerta nivelAlerta,
            LocalDateTime fechaInicio,
            LocalDateTime ultimaSenalEn,
            Double ultimaLatitud,
            Double ultimaLongitud,
            Double velocidadKmh,
            UUID alertaAbiertaId) {
    }

    record ConductorSinViaje(ConductorResumen conductor, LocalDateTime fechaUltimoViaje) {
    }

    record EstadoFlota(
            LocalDateTime actualizadoEn,
            Map<EstadoMonitoreo, Integer> conteoPorEstado,
            List<ConductorEnViaje> conductoresEnViaje,
            List<ConductorSinViaje> conductoresSinViaje) {
    }

    record AlertaVista(AlertaCritica alerta, ConductorResumen conductor) {
    }

    record EntradaLineaTiempo(LocalDateTime fechaHora, String tipo, String descripcion) {
    }

    record AlertaDetalleVista(
            AlertaCritica alerta,
            ConductorResumen conductor,
            LocalDateTime fechaInicioViaje,
            LocalDateTime ultimaSenalEn,
            List<EntradaLineaTiempo> lineaTiempo) {
    }

    /** Un evento del historial con conductor y estado de atencion. */
    record EventoHistorial(EventoFatiga evento, ConductorResumen conductor, String atencion) {
    }

    record PaginaEventos(
            int pagina,
            int tamano,
            long totalElementos,
            int totalPaginas,
            List<EventoHistorial> contenido) {
    }
}
