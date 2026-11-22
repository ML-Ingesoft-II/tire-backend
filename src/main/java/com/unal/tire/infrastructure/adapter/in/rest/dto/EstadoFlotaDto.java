package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.EstadoFlota;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Estado de la flota para el panel del despachador ({@code EstadoFlotaResponse}).
 */
public record EstadoFlotaDto(
        LocalDateTime actualizadoEn,
        Map<EstadoMonitoreo, Integer> conteoPorEstado,
        List<ConductorEnViajeDto> conductoresEnViaje,
        List<ConductorSinViajeDto> conductoresSinViaje) {

    public static EstadoFlotaDto desde(EstadoFlota estado) {
        return new EstadoFlotaDto(
                estado.actualizadoEn(),
                estado.conteoPorEstado(),
                estado.conductoresEnViaje().stream().map(ConductorEnViajeDto::desde).toList(),
                estado.conductoresSinViaje().stream().map(ConductorSinViajeDto::desde).toList());
    }
}
