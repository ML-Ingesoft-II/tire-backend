package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.AlertaVista;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacion de salida de una alerta critica ({@code AlertaResponse}).
 */
public record AlertaResponseDto(
        UUID id,
        UUID viajeId,
        UUID eventoId,
        ConductorResumenDto conductor,
        TipoEvento tipoEvento,
        Double duracionSegundos,
        LocalDateTime fechaEvento,
        LocalDateTime fechaApertura,
        Double latitud,
        Double longitud,
        boolean atendida,
        LocalDateTime fechaAtencion,
        UsuarioResumenDto atendidaPor,
        String notasDespachador) {

    public static AlertaResponseDto desde(AlertaVista vista) {
        AlertaCritica a = vista.alerta();
        Ubicacion u = a.getUltimaUbicacion();
        return new AlertaResponseDto(
                a.getId(),
                a.getViajeId(),
                a.getEventoId(),
                ConductorResumenDto.desde(vista.conductor()),
                a.getTipoEvento(),
                a.getDuracionSegundos(),
                a.getFechaEvento(),
                a.getFechaApertura(),
                u != null ? u.latitud() : null,
                u != null ? u.longitud() : null,
                a.isAtendida(),
                a.getFechaAtencion(),
                UsuarioResumenDto.de(a.getAtendidaPorId(), a.getAtendidaPorNombre()),
                a.getNotasDespachador());
    }
}
