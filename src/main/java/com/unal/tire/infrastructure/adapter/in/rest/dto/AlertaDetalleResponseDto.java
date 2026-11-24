package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.AlertaDetalleVista;
import com.unal.tire.application.port.in.GestionAlertasUseCase.AlertaVista;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Detalle de una alerta con su linea de tiempo ({@code AlertaDetalleResponse}).
 * Repite los campos de {@code AlertaResponse} mas los especificos del detalle.
 */
public record AlertaDetalleResponseDto(
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
        String notasDespachador,
        LocalDateTime fechaInicioViaje,
        LocalDateTime ultimaSenalEn,
        List<EntradaLineaTiempoDto> lineaTiempo) {

    public static AlertaDetalleResponseDto desde(AlertaDetalleVista vista) {
        AlertaCritica a = vista.alerta();
        Ubicacion u = a.getUltimaUbicacion();
        return new AlertaDetalleResponseDto(
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
                a.getNotasDespachador(),
                vista.fechaInicioViaje(),
                vista.ultimaSenalEn(),
                vista.lineaTiempo().stream().map(EntradaLineaTiempoDto::desde).toList());
    }

    /** Variante auxiliar si solo se dispone de la vista basica. */
    public static AlertaDetalleResponseDto desdeBasica(AlertaVista vista) {
        AlertaResponseDto base = AlertaResponseDto.desde(vista);
        return new AlertaDetalleResponseDto(
                base.id(), base.viajeId(), base.eventoId(), base.conductor(), base.tipoEvento(),
                base.duracionSegundos(), base.fechaEvento(), base.fechaApertura(), base.latitud(),
                base.longitud(), base.atendida(), base.fechaAtencion(), base.atendidaPor(),
                base.notasDespachador(), null, null, List.of());
    }
}
